package com.codereview.backend.execution.cpp;

import com.codereview.backend.entity.TestCaseEntity;
import com.codereview.backend.execution.ExecutionRequest;
import com.codereview.backend.execution.ExecutionResult;
import com.codereview.backend.execution.TestCaseExecutionDetail;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Component
@RequiredArgsConstructor
public class CppExecutionEngine {

    private final CppCompilerService compilerService;

    private static final int MAX_OUTPUT_CHARS = 10000;
    private static final Set<String> SENSITIVE_ENV_KEYS = Set.of(
            "GEMINI_API_KEY", "API_KEY", "SECRET", "TOKEN", "PASSWORD", "PRIVATE_KEY"
    );
    private static final Pattern TOKEN_PATTERN = Pattern.compile("[^\\s,\\[\\]]+");

    public boolean hasMainMethod(String code) {
        if (code == null) return false;
        // Matches int main(...), int main ( ), void main(...), auto main(...)
        return Pattern.compile("(int|void|auto)\\s+main\\s*\\(").matcher(code).find();
    }

    public ExecutionResult execute(ExecutionRequest request) {
        String code = request.getCode();
        List<TestCaseEntity> testCases = request.getTestCases();
        if (testCases == null) {
            testCases = List.of();
        }

        if (code == null || code.trim().isEmpty()) {
            return ExecutionResult.builder()
                    .status("COMPILATION_ERROR")
                    .passedTestCases(0)
                    .totalTestCases(testCases.size())
                    .compilationOutput("Source code is empty")
                    .userFacingSummary("Compilation failed: Source code is empty")
                    .testCaseDetails(List.of())
                    .build();
        }

        if (!hasMainMethod(code)) {
            return ExecutionResult.builder()
                    .status("COMPILATION_ERROR")
                    .passedTestCases(0)
                    .totalTestCases(testCases.size())
                    .compilationOutput("Missing entry point: 'main' function is required to compile and execute C++ programs.")
                    .userFacingSummary("Compilation failed: Missing 'main' function entry point.")
                    .testCaseDetails(List.of())
                    .build();
        }

        if (!compilerService.isCompilerAvailable()) {
            return ExecutionResult.builder()
                    .status("NOT_APPLICABLE")
                    .applicable(false)
                    .passedTestCases(0)
                    .totalTestCases(testCases.size())
                    .compilationOutput("C++ compiler is not configured or available in this environment.")
                    .userFacingSummary("Execution not available: No C++ compiler detected.")
                    .testCaseDetails(List.of())
                    .build();
        }

        long timeLimitMs = request.getTimeLimitMs() > 0 ? request.getTimeLimitMs() : 2000L;
        return runCppTestCases(code, testCases, timeLimitMs);
    }

    public ExecutionResult executeFreeform(String code, long timeLimitMs) {
        if (code == null || code.trim().isEmpty()) {
            return ExecutionResult.builder()
                    .status("COMPILATION_ERROR")
                    .applicable(true)
                    .compilationOutput("Source code is empty")
                    .userFacingSummary("Compilation failed: Source code is empty")
                    .build();
        }

        if (!hasMainMethod(code)) {
            return ExecutionResult.builder()
                    .status("NOT_APPLICABLE")
                    .applicable(false)
                    .applicabilityReason("This snippet does not contain a runnable main entry point. Static analysis is still available.")
                    .userFacingSummary("Execution not applicable: No runnable main entry point found.")
                    .build();
        }

        if (!compilerService.isCompilerAvailable()) {
            return ExecutionResult.builder()
                    .status("NOT_APPLICABLE")
                    .applicable(false)
                    .applicabilityReason("No C++ compiler (g++, clang++, or MSVC) was detected on the host system.")
                    .userFacingSummary("Execution not available: C++ compiler not found.")
                    .build();
        }

        long actualLimit = timeLimitMs > 0 ? timeLimitMs : 2000L;
        return runFreeformCppCode(code, actualLimit);
    }

    private record ProcessExecutionResult(
            boolean timedOut,
            int exitCode,
            String stdout,
            String stderr,
            long durationMs,
            String executionError
    ) {}

    private void terminateProcessTree(Process process) {
        if (process == null) return;
        List<ProcessHandle> descendants = List.of();
        try {
            descendants = process.descendants().toList();
        } catch (Exception ignored) {}

        for (ProcessHandle ph : descendants) {
            try {
                ph.destroyForcibly();
            } catch (Exception ignored) {}
        }
        try {
            process.destroyForcibly();
        } catch (Exception ignored) {}

        try {
            process.waitFor(1, TimeUnit.SECONDS);
        } catch (Exception ignored) {}

        if (!descendants.isEmpty()) {
            try {
                CompletableFuture<?>[] exitFutures = descendants.stream()
                        .map(ph -> ph.onExit().exceptionally(ex -> ph))
                        .toArray(CompletableFuture[]::new);
                CompletableFuture.allOf(exitFutures).get(500, TimeUnit.MILLISECONDS);
            } catch (Exception ignored) {}
        }
    }

    private void closeQuietly(Closeable c) {
        if (c != null) {
            try {
                c.close();
            } catch (IOException ignored) {}
        }
    }

    private ProcessExecutionResult executeProcess(File exeFile, File workDir, String stdinInput, long timeLimitMs) {
        ProcessBuilder pb = new ProcessBuilder(exeFile.getAbsolutePath());
        if (workDir != null) {
            pb.directory(workDir);
        }
        sanitizeEnvironment(pb);

        Process process = null;
        ExecutorService streamPool = Executors.newFixedThreadPool(3);
        Future<String> stdoutFuture = null;
        Future<String> stderrFuture = null;
        Future<?> stdinFuture = null;
        long startTime = System.currentTimeMillis();
        boolean timedOut = false;
        int exitCode = -1;
        String stdout = "";
        String stderr = "";

        try {
            process = pb.start();
            final Process procRef = process;

            // 1. Immediately start consuming stdout and stderr before writing stdin
            stdoutFuture = streamPool.submit(() -> readStreamWithLimit(procRef.getInputStream(), MAX_OUTPUT_CHARS));
            stderrFuture = streamPool.submit(() -> readStreamWithLimit(procRef.getErrorStream(), MAX_OUTPUT_CHARS));

            // 2. Write stdin asynchronously so a blocked write cannot bypass process timeout
            if (stdinInput != null && !stdinInput.isEmpty()) {
                stdinFuture = streamPool.submit(() -> {
                    try (OutputStream os = procRef.getOutputStream()) {
                        os.write(stdinInput.getBytes(StandardCharsets.UTF_8));
                        os.flush();
                    } catch (IOException ignored) {
                        // Process may exit before or while stdin is being written
                    }
                });
            } else {
                // Close stdin immediately if no input required
                closeQuietly(process.getOutputStream());
            }

            // 3. Enforce bounded timeout
            boolean finished = process.waitFor(timeLimitMs, TimeUnit.MILLISECONDS);
            long duration = Math.max(1L, System.currentTimeMillis() - startTime);

            if (!finished) {
                timedOut = true;
                terminateProcessTree(process);
            } else {
                exitCode = process.exitValue();

                // Safely wait for stdin completion with a short bounded timeout; cancel if incomplete
                if (stdinFuture != null) {
                    try {
                        stdinFuture.get(250, TimeUnit.MILLISECONDS);
                    } catch (Exception e) {
                        stdinFuture.cancel(true);
                    }
                }

                // Wait briefly for stream readers to finish draining EOF
                try {
                    stdout = stdoutFuture.get(1, TimeUnit.SECONDS);
                } catch (Exception e) {
                    stdout = "";
                }
                try {
                    stderr = stderrFuture.get(1, TimeUnit.SECONDS);
                } catch (Exception e) {
                    stderr = "";
                }
            }

            return new ProcessExecutionResult(timedOut, exitCode, stdout.trim(), stderr.trim(), duration, null);

        } catch (Exception e) {
            long duration = Math.max(1L, System.currentTimeMillis() - startTime);
            log.error("C++ process execution error: {}", e.getMessage(), e);
            return new ProcessExecutionResult(false, -1, "", "", duration, e.getMessage());
        } finally {
            if (stdinFuture != null) {
                stdinFuture.cancel(true);
            }
            if (process != null) {
                closeQuietly(process.getOutputStream());
                closeQuietly(process.getInputStream());
                closeQuietly(process.getErrorStream());
                if (timedOut || process.isAlive()) {
                    terminateProcessTree(process);
                }
            }
            streamPool.shutdownNow();
            try {
                streamPool.awaitTermination(500, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private ExecutionResult runFreeformCppCode(String code, long timeLimitMs) {
        Path tempDir = null;
        try {
            tempDir = Files.createTempDirectory("cpp_freeform_exec_");
            File sourceFile = new File(tempDir.toFile(), "solution.cpp");
            Files.writeString(sourceFile.toPath(), code, StandardCharsets.UTF_8);

            String exeName = System.getProperty("os.name").toLowerCase().contains("win") ? "solution.exe" : "solution";
            File exeFile = new File(tempDir.toFile(), exeName);

            CppCompilerService.CompilationResult compileRes = compilerService.compile(sourceFile, exeFile);
            if (!compileRes.success() || !exeFile.exists()) {
                String err = compileRes.output() != null && !compileRes.output().isEmpty()
                        ? compileRes.output()
                        : "Unknown C++ compilation error.";
                return ExecutionResult.builder()
                        .status("COMPILATION_ERROR")
                        .applicable(true)
                        .compilationOutput(err)
                        .userFacingSummary("Compilation Error:\n" + err)
                        .build();
            }

            ProcessExecutionResult execRes = executeProcess(exeFile, tempDir.toFile(), null, timeLimitMs);

            if (execRes.executionError() != null) {
                return ExecutionResult.builder()
                        .status("RUNTIME_ERROR")
                        .applicable(true)
                        .executionTimeMs(execRes.durationMs())
                        .stderr(execRes.executionError())
                        .userFacingSummary("Execution Error: " + execRes.executionError())
                        .build();
            }

            if (execRes.timedOut()) {
                return ExecutionResult.builder()
                        .status("TIME_LIMIT_EXCEEDED")
                        .applicable(true)
                        .timedOut(true)
                        .executionTimeMs(timeLimitMs)
                        .userFacingSummary("Time Limit Exceeded (> " + timeLimitMs + " ms)")
                        .build();
            }

            if (execRes.exitCode() != 0) {
                String errMsg = !execRes.stderr().isEmpty() ? execRes.stderr() : "Process exited with non-zero exit code: " + execRes.exitCode();
                return ExecutionResult.builder()
                        .status("RUNTIME_ERROR")
                        .applicable(true)
                        .executionTimeMs(execRes.durationMs())
                        .stdout(execRes.stdout())
                        .stderr(errMsg)
                        .userFacingSummary("Runtime Error (exit code " + execRes.exitCode() + "): " + errMsg)
                        .build();
            }

            return ExecutionResult.builder()
                    .status("PASSED")
                    .applicable(true)
                    .executionTimeMs(execRes.durationMs())
                    .stdout(execRes.stdout())
                    .stderr(execRes.stderr())
                    .userFacingSummary("Execution completed cleanly in " + execRes.durationMs() + " ms.")
                    .build();

        } catch (Exception e) {
            log.error("C++ Freeform execution failure: {}", e.getMessage(), e);
            return ExecutionResult.builder()
                    .status("RUNTIME_ERROR")
                    .applicable(true)
                    .stderr(e.getMessage())
                    .userFacingSummary("Execution Error: " + e.getMessage())
                    .build();
        } finally {
            if (tempDir != null) {
                deleteDir(tempDir.toFile());
            }
        }
    }

    private ExecutionResult runCppTestCases(String code, List<TestCaseEntity> testCases, long timeLimitMs) {
        Path tempDir = null;
        try {
            tempDir = Files.createTempDirectory("cpp_challenge_exec_");
            File sourceFile = new File(tempDir.toFile(), "solution.cpp");
            Files.writeString(sourceFile.toPath(), code, StandardCharsets.UTF_8);

            String exeName = System.getProperty("os.name").toLowerCase().contains("win") ? "solution.exe" : "solution";
            File exeFile = new File(tempDir.toFile(), exeName);

            CppCompilerService.CompilationResult compileRes = compilerService.compile(sourceFile, exeFile);
            if (!compileRes.success() || !exeFile.exists()) {
                String err = compileRes.output() != null && !compileRes.output().isEmpty()
                        ? compileRes.output()
                        : "C++ compilation failed.";
                return ExecutionResult.builder()
                        .status("COMPILATION_ERROR")
                        .passedTestCases(0)
                        .totalTestCases(testCases.size())
                        .compilationOutput(err)
                        .userFacingSummary("Compilation Error:\n" + err)
                        .testCaseDetails(List.of())
                        .build();
            }

            if (testCases.isEmpty()) {
                return ExecutionResult.builder()
                        .status("PASSED")
                        .passedTestCases(0)
                        .totalTestCases(0)
                        .executionTimeMs(compileRes.compilationTimeMs())
                        .memoryUsedKb(12000L)
                        .compilationOutput("Build Successful")
                        .userFacingSummary("No test cases defined for this problem.")
                        .testCaseDetails(List.of())
                        .build();
            }

            int passedCount = 0;
            List<TestCaseExecutionDetail> details = new ArrayList<>();
            long totalExecutionTime = 0;
            boolean hasTimeout = false;
            boolean hasRuntimeError = false;

            for (TestCaseEntity tc : testCases) {
                String rawInput = tc.getInput() != null ? tc.getInput() : "";
                String stdinContent = prepareStdinInput(rawInput);

                ProcessExecutionResult execRes = executeProcess(exeFile, tempDir.toFile(), stdinContent, timeLimitMs);
                totalExecutionTime += execRes.durationMs();

                boolean passed = false;
                String actualOutputStr = "";
                String failureReason = null;

                if (execRes.executionError() != null) {
                    hasRuntimeError = true;
                    failureReason = "Execution error: " + execRes.executionError();
                    actualOutputStr = "Error";
                } else if (execRes.timedOut()) {
                    hasTimeout = true;
                    failureReason = "Time Limit Exceeded (> " + timeLimitMs + " ms)";
                    actualOutputStr = "Time Limit Exceeded";
                } else if (execRes.exitCode() != 0) {
                    hasRuntimeError = true;
                    String stderr = execRes.stderr();
                    failureReason = "Runtime Exception (exit code " + execRes.exitCode() + ")" + (!stderr.isEmpty() ? ": " + stderr : "");
                    actualOutputStr = "Runtime Error";
                } else {
                    actualOutputStr = execRes.stdout();
                    String expectedNormalized = normalizeOutput(tc.getExpectedOutput());
                    String actualNormalized = normalizeOutput(actualOutputStr);

                    if (matchesExpected(expectedNormalized, actualNormalized)) {
                        passed = true;
                        passedCount++;
                    } else {
                        failureReason = String.format("Expected: %s, Got: %s", tc.getExpectedOutput(), actualOutputStr);
                    }
                }

                if (!passed && tc.isHidden()) {
                    failureReason = "Hidden test case failed.";
                }

                details.add(TestCaseExecutionDetail.builder()
                        .testCaseId(tc.getId())
                        .passed(passed)
                        .input(tc.isHidden() ? "[Hidden Case]" : tc.getInput())
                        .expectedOutput(tc.isHidden() ? "[Hidden Case]" : tc.getExpectedOutput())
                        .actualOutput(tc.isHidden() ? "[Hidden Case]" : actualOutputStr)
                        .failureReason(failureReason)
                        .isHidden(tc.isHidden())
                        .build());
            }

            boolean allPassed = passedCount == testCases.size();
            String status;
            if (allPassed) {
                status = "PASSED";
            } else if (hasTimeout && passedCount == 0) {
                status = "TIME_LIMIT_EXCEEDED";
            } else if (hasRuntimeError && passedCount == 0) {
                status = "RUNTIME_ERROR";
            } else {
                status = "FAILED";
            }

            String summary = allPassed ?
                    String.format("✓ Accepted. Solution passed all %d/%d test cases.", passedCount, testCases.size()) :
                    String.format("✕ Wrong Answer / Test Failure. Passed %d/%d test cases.", passedCount, testCases.size());

            return ExecutionResult.builder()
                    .status(status)
                    .passedTestCases(passedCount)
                    .totalTestCases(testCases.size())
                    .executionTimeMs(Math.max(1L, totalExecutionTime))
                    .memoryUsedKb(12000L)
                    .compilationOutput("Build Successful")
                    .userFacingSummary(summary)
                    .testCaseDetails(details)
                    .build();

        } catch (Exception e) {
            log.error("C++ Challenge execution error: {}", e.getMessage(), e);
            return ExecutionResult.builder()
                    .status("RUNTIME_ERROR")
                    .passedTestCases(0)
                    .totalTestCases(testCases.size())
                    .compilationOutput(e.getMessage())
                    .userFacingSummary("Execution Error: " + e.getMessage())
                    .testCaseDetails(List.of())
                    .build();
        } finally {
            if (tempDir != null) {
                deleteDir(tempDir.toFile());
            }
        }
    }

    private String prepareStdinInput(String input) {
        if (input == null) return "";
        // Clean key=value prefixes e.g. "nums = [2,7,11,15], target = 9" -> "[2,7,11,15]\n9\n"
        String clean = input.replaceAll("[a-zA-Z0-9_]+\\s*=\\s*", "").trim();
        // Return with newline for std::cin
        return clean + "\n";
    }

    private boolean matchesExpected(String expected, String actual) {
        if (expected == null && actual == null) return true;
        if (expected == null || actual == null) return false;

        String expTrim = expected.trim();
        String actTrim = actual.trim();
        if (expTrim.equalsIgnoreCase(actTrim)) return true;

        if (expTrim.isEmpty() || actTrim.isEmpty()) {
            return false;
        }

        List<String> expTokens = tokenizeOutput(expTrim);
        List<String> actTokens = tokenizeOutput(actTrim);

        if (expTokens.isEmpty() && actTokens.isEmpty()) {
            return expTrim.replaceAll("\\s+", "").equalsIgnoreCase(actTrim.replaceAll("\\s+", ""));
        }

        if (expTokens.size() != actTokens.size()) {
            return false;
        }

        for (int i = 0; i < expTokens.size(); i++) {
            if (!expTokens.get(i).equalsIgnoreCase(actTokens.get(i))) {
                return false;
            }
        }

        return true;
    }

    private List<String> tokenizeOutput(String str) {
        if (str == null || str.trim().isEmpty()) {
            return List.of();
        }
        List<String> tokens = new ArrayList<>();
        Matcher m = TOKEN_PATTERN.matcher(str);
        while (m.find()) {
            tokens.add(m.group());
        }
        return tokens;
    }

    private String normalizeOutput(String str) {
        if (str == null) return "";
        return str.trim()
                .replaceAll("\\s*,\\s*", ",")
                .replaceAll("[\"']", "");
    }

    private void sanitizeEnvironment(ProcessBuilder pb) {
        Map<String, String> env = pb.environment();
        for (String key : new ArrayList<>(env.keySet())) {
            for (String sensitive : SENSITIVE_ENV_KEYS) {
                if (key.toUpperCase().contains(sensitive)) {
                    env.remove(key);
                }
            }
        }
    }

    private String readStreamWithLimit(InputStream is, int maxChars) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            StringBuilder sb = new StringBuilder();
            char[] buffer = new char[1024];
            int read;
            boolean truncated = false;
            while ((read = reader.read(buffer)) != -1) {
                if (!truncated) {
                    if (sb.length() + read >= maxChars) {
                        int keep = Math.max(0, maxChars - sb.length());
                        sb.append(buffer, 0, keep);
                        sb.append("\n... [Output truncated at ").append(maxChars).append(" characters]");
                        truncated = true;
                    } else {
                        sb.append(buffer, 0, read);
                    }
                }
                // Continue reading until EOF even after truncation to prevent process blocking on stdout/stderr
            }
            return sb.toString();
        } catch (Exception e) {
            return "";
        }
    }

    private void deleteDir(File dir) {
        if (dir == null || !dir.exists()) return;
        File[] files = dir.listFiles();
        if (files != null) {
            for (File f : files) {
                if (f.isDirectory()) {
                    deleteDir(f);
                } else {
                    if (!f.delete()) {
                        f.deleteOnExit();
                    }
                }
            }
        }
        if (!dir.delete()) {
            dir.deleteOnExit();
        }
    }
}
