package com.codereview.backend.execution.cpp;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

@Slf4j
@Service
public class CppCompilerService {

    public enum CompilerType {
        GCC,
        CLANG,
        MSVC,
        NONE
    }

    @Getter
    private CompilerType detectedCompiler = CompilerType.NONE;

    @Getter
    private String compilerCommand = null;

    @Getter
    private String vcvarsallPath = null;

    public record CompilationResult(
            boolean success,
            String output,
            File executableFile,
            long compilationTimeMs
    ) {}

    public CppCompilerService(
            @Value("${cpp.compiler.path:}") String customCompilerPath,
            @Value("${cpp.vcvarsall.path:}") String customVcvarsallPath) {
        initializeCompiler(customCompilerPath, customVcvarsallPath);
    }

    private void initializeCompiler(String customPath, String customVcvars) {
        // 1. If custom path provided, check it
        if (customPath != null && !customPath.trim().isEmpty()) {
            File f = new File(customPath.trim());
            if (f.exists()) {
                String name = f.getName().toLowerCase();
                if (name.contains("clang")) {
                    detectedCompiler = CompilerType.CLANG;
                    compilerCommand = f.getAbsolutePath();
                    log.info("Configured custom Clang C++ compiler: {}", compilerCommand);
                    return;
                } else if (name.contains("g++") || name.contains("gcc")) {
                    detectedCompiler = CompilerType.GCC;
                    compilerCommand = f.getAbsolutePath();
                    log.info("Configured custom GCC C++ compiler: {}", compilerCommand);
                    return;
                } else if (name.contains("cl.exe")) {
                    detectedCompiler = CompilerType.MSVC;
                    compilerCommand = f.getAbsolutePath();
                    if (customVcvars != null && new File(customVcvars).exists()) {
                        vcvarsallPath = customVcvars;
                    }
                    log.info("Configured custom MSVC compiler: {}", compilerCommand);
                    return;
                }
            }
        }

        // 2. Check for g++ in PATH
        if (isExecutableAvailable("g++")) {
            detectedCompiler = CompilerType.GCC;
            compilerCommand = "g++";
            log.info("Detected GCC C++ compiler (g++) in PATH.");
            return;
        }

        // 3. Check for clang++ in PATH
        if (isExecutableAvailable("clang++")) {
            detectedCompiler = CompilerType.CLANG;
            compilerCommand = "clang++";
            log.info("Detected Clang C++ compiler (clang++) in PATH.");
            return;
        }

        // 4. Check for MSVC vcvarsall / cl.exe (via vswhere or standard locations)
        String vswherePath = findVcvarsallViaVswhere();
        String[] candidateVcvars = {
                customVcvars,
                vswherePath,
                "C:\\Program Files\\Microsoft Visual Studio\\2022\\Community\\VC\\Auxiliary\\Build\\vcvarsall.bat",
                "C:\\Program Files\\Microsoft Visual Studio\\2022\\Professional\\VC\\Auxiliary\\Build\\vcvarsall.bat",
                "C:\\Program Files\\Microsoft Visual Studio\\2022\\Enterprise\\VC\\Auxiliary\\Build\\vcvarsall.bat",
                "C:\\Program Files (x86)\\Microsoft Visual Studio\\2019\\BuildTools\\VC\\Auxiliary\\Build\\vcvarsall.bat",
                "C:\\Program Files (x86)\\Microsoft Visual Studio\\2019\\Community\\VC\\Auxiliary\\Build\\vcvarsall.bat"
        };

        for (String candidate : candidateVcvars) {
            if (candidate != null && !candidate.trim().isEmpty()) {
                File vcvarsFile = new File(candidate.trim());
                if (vcvarsFile.exists()) {
                    vcvarsallPath = vcvarsFile.getAbsolutePath();
                    detectedCompiler = CompilerType.MSVC;
                    compilerCommand = "cl.exe";
                    log.info("Detected MSVC C++ compiler via vcvarsall at: {}", vcvarsallPath);
                    return;
                }
            }
        }

        // 5. Check if cl.exe is directly in PATH (e.g. running from Developer Command Prompt)
        if (isExecutableAvailable("cl.exe") || isExecutableAvailable("cl")) {
            detectedCompiler = CompilerType.MSVC;
            compilerCommand = "cl.exe";
            log.info("Detected MSVC C++ compiler (cl.exe) in PATH.");
            return;
        }

        log.warn("No C++ compiler (GCC, Clang, or MSVC) detected in the current environment.");
        detectedCompiler = CompilerType.NONE;
    }

    public boolean isCompilerAvailable() {
        return detectedCompiler != CompilerType.NONE;
    }

    private static final int MAX_OUTPUT_CHARS = 20000;

    public CompilationResult compile(File sourceFile, File executableFile) {
        if (!isCompilerAvailable()) {
            return new CompilationResult(
                    false,
                    "No C++ compiler (g++, clang++, or MSVC cl.exe) found on this system. Please install a C++ compiler to compile and execute C++ programs.",
                    null,
                    0L
            );
        }

        long startTime = System.currentTimeMillis();
        Process process = null;
        ExecutorService streamPool = Executors.newSingleThreadExecutor();
        File workDir = sourceFile != null ? sourceFile.getParentFile() : null;

        try {
            ProcessBuilder pb;
            if (detectedCompiler == CompilerType.MSVC) {
                // Compile using MSVC cl.exe
                String cmd;
                if (vcvarsallPath != null) {
                    cmd = String.format("call \"%s\" x64 >nul 2>&1 && cl.exe /nologo /O2 /EHsc /std:c++17 \"%s\" /Fe:\"%s\"",
                            vcvarsallPath, sourceFile.getAbsolutePath(), executableFile.getAbsolutePath());
                } else {
                    cmd = String.format("cl.exe /nologo /O2 /EHsc /std:c++17 \"%s\" /Fe:\"%s\"",
                            sourceFile.getAbsolutePath(), executableFile.getAbsolutePath());
                }
                pb = new ProcessBuilder("cmd.exe", "/c", cmd);
            } else {
                // GCC or Clang
                List<String> command = new ArrayList<>();
                command.add(compilerCommand);
                command.add("-O2");
                command.add("-std=c++17");
                command.add("-Wall");
                command.add(sourceFile.getAbsolutePath());
                command.add("-o");
                command.add(executableFile.getAbsolutePath());
                pb = new ProcessBuilder(command);
            }

            if (workDir != null) {
                pb.directory(workDir);
            }
            pb.redirectErrorStream(true);

            process = pb.start();
            final Process procRef = process;

            // Start output reading immediately and asynchronously
            Future<String> outputFuture = streamPool.submit(() -> readStreamWithLimit(procRef.getInputStream(), MAX_OUTPUT_CHARS));

            // Enforce 30-second timeout independently of output reading
            boolean finished = process.waitFor(30, TimeUnit.SECONDS);
            long duration = System.currentTimeMillis() - startTime;

            if (!finished) {
                terminateProcess(process);
                return new CompilationResult(false, "Compilation timed out (> 30s).", null, duration);
            }

            String output = "";
            try {
                output = outputFuture.get(1, TimeUnit.SECONDS);
            } catch (Exception e) {
                output = "";
            }

            int exitCode = process.exitValue();
            boolean success = (exitCode == 0) && executableFile.exists();
            String outStr = output != null ? output.trim() : "";

            return new CompilationResult(success, outStr, success ? executableFile : null, duration);

        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("Compilation process error: {}", e.getMessage(), e);
            return new CompilationResult(false, "Compilation error: " + e.getMessage(), null, duration);
        } finally {
            if (process != null) {
                closeQuietly(process.getInputStream());
                closeQuietly(process.getErrorStream());
                closeQuietly(process.getOutputStream());
                if (process.isAlive()) {
                    terminateProcess(process);
                }
            }
            streamPool.shutdownNow();
            try {
                streamPool.awaitTermination(500, TimeUnit.MILLISECONDS);
            } catch (InterruptedException ignored) {}

            // Clean up temporary MSVC .obj files on every path (success, failure, timeout, exception)
            if (detectedCompiler == CompilerType.MSVC) {
                cleanUpMsvcObj(workDir, sourceFile);
            }
        }
    }

    public CompilationResult checkSyntax(File sourceFile) {
        if (!isCompilerAvailable()) {
            return new CompilationResult(false, "No C++ compiler available for syntax validation.", null, 0L);
        }

        long startTime = System.currentTimeMillis();
        Process process = null;
        ExecutorService streamPool = Executors.newSingleThreadExecutor();
        File workDir = sourceFile != null ? sourceFile.getParentFile() : null;

        try {
            ProcessBuilder pb;
            if (detectedCompiler == CompilerType.MSVC) {
                String cmd;
                if (vcvarsallPath != null) {
                    cmd = String.format("call \"%s\" x64 >nul 2>&1 && cl.exe /nologo /Zs /EHsc /std:c++17 \"%s\"",
                            vcvarsallPath, sourceFile.getAbsolutePath());
                } else {
                    cmd = String.format("cl.exe /nologo /Zs /EHsc /std:c++17 \"%s\"", sourceFile.getAbsolutePath());
                }
                pb = new ProcessBuilder("cmd.exe", "/c", cmd);
            } else {
                List<String> command = new ArrayList<>();
                command.add(compilerCommand);
                command.add("-fsyntax-only");
                command.add("-std=c++17");
                command.add("-Wall");
                command.add(sourceFile.getAbsolutePath());
                pb = new ProcessBuilder(command);
            }

            if (workDir != null) {
                pb.directory(workDir);
            }
            pb.redirectErrorStream(true);

            process = pb.start();
            final Process procRef = process;

            // Start output reading immediately and asynchronously
            Future<String> outputFuture = streamPool.submit(() -> readStreamWithLimit(procRef.getInputStream(), MAX_OUTPUT_CHARS));

            // Enforce 15-second timeout independently of output reading
            boolean finished = process.waitFor(15, TimeUnit.SECONDS);
            long duration = System.currentTimeMillis() - startTime;

            if (!finished) {
                terminateProcess(process);
                return new CompilationResult(false, "Syntax validation timed out.", null, duration);
            }

            String output = "";
            try {
                output = outputFuture.get(1, TimeUnit.SECONDS);
            } catch (Exception e) {
                output = "";
            }

            boolean success = (process.exitValue() == 0);
            return new CompilationResult(success, output != null ? output.trim() : "", null, duration);

        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            return new CompilationResult(false, "Syntax check error: " + e.getMessage(), null, duration);
        } finally {
            if (process != null) {
                closeQuietly(process.getInputStream());
                closeQuietly(process.getErrorStream());
                closeQuietly(process.getOutputStream());
                if (process.isAlive()) {
                    terminateProcess(process);
                }
            }
            streamPool.shutdownNow();
            try {
                streamPool.awaitTermination(500, TimeUnit.MILLISECONDS);
            } catch (InterruptedException ignored) {}
        }
    }

    private String findVcvarsallViaVswhere() {
        File vswhere = new File("C:\\Program Files (x86)\\Microsoft Visual Studio\\Installer\\vswhere.exe");
        if (!vswhere.exists()) {
            return null;
        }
        ExecutorService pool = Executors.newSingleThreadExecutor();
        Process p = null;
        try {
            ProcessBuilder pb = new ProcessBuilder(
                    vswhere.getAbsolutePath(),
                    "-latest",
                    "-products", "*",
                    "-requires", "Microsoft.VisualStudio.Component.VC.Tools.x86.x64",
                    "-property", "installationPath"
            );
            pb.redirectErrorStream(true);
            p = pb.start();
            final Process procRef = p;

            Future<String> outputFuture = pool.submit(() -> readStreamWithLimit(procRef.getInputStream(), 4096));

            boolean finished = p.waitFor(5, TimeUnit.SECONDS);
            if (!finished) {
                terminateProcess(p);
                return null;
            }

            if (p.exitValue() == 0) {
                String installPath = "";
                try {
                    installPath = outputFuture.get(1, TimeUnit.SECONDS).trim();
                } catch (Exception ignored) {}
                if (!installPath.isEmpty()) {
                    File candidate = new File(installPath, "VC\\Auxiliary\\Build\\vcvarsall.bat");
                    if (candidate.exists()) {
                        return candidate.getAbsolutePath();
                    }
                }
            }
        } catch (Exception e) {
            log.debug("vswhere resolution failed: {}", e.getMessage());
        } finally {
            if (p != null) {
                closeQuietly(p.getInputStream());
                closeQuietly(p.getErrorStream());
                closeQuietly(p.getOutputStream());
                if (p.isAlive()) {
                    terminateProcess(p);
                }
            }
            pool.shutdownNow();
            try {
                pool.awaitTermination(500, TimeUnit.MILLISECONDS);
            } catch (InterruptedException ignored) {}
        }
        return null;
    }

    private boolean isExecutableAvailable(String command) {
        if (command == null || command.trim().isEmpty()) {
            return false;
        }
        ExecutorService pool = Executors.newSingleThreadExecutor();
        Process p = null;
        try {
            List<String> cmdList = new ArrayList<>();
            cmdList.add(command);
            String lower = command.toLowerCase();
            if (lower.equals("cl") || lower.equals("cl.exe") || lower.endsWith("\\cl.exe") || lower.endsWith("/cl.exe")) {
                cmdList.add("/?");
            } else {
                cmdList.add("--version");
            }

            ProcessBuilder pb = new ProcessBuilder(cmdList);
            pb.redirectErrorStream(true);
            p = pb.start();
            final Process procRef = p;

            // Drain output in background to prevent process from blocking on stdout/stderr pipe
            Future<String> drainFuture = pool.submit(() -> readStreamWithLimit(procRef.getInputStream(), 4096));

            boolean done = p.waitFor(3, TimeUnit.SECONDS);
            if (!done) {
                terminateProcess(p);
                return false;
            }

            try {
                drainFuture.get(500, TimeUnit.MILLISECONDS);
            } catch (Exception ignored) {}

            return p.exitValue() == 0;
        } catch (Exception e) {
            return false;
        } finally {
            if (p != null) {
                closeQuietly(p.getInputStream());
                closeQuietly(p.getErrorStream());
                closeQuietly(p.getOutputStream());
                if (p.isAlive()) {
                    terminateProcess(p);
                }
            }
            pool.shutdownNow();
            try {
                pool.awaitTermination(500, TimeUnit.MILLISECONDS);
            } catch (InterruptedException ignored) {}
        }
    }

    private String readStreamWithLimit(InputStream is, int maxChars) {
        if (is == null) return "";
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
                // Continue draining until EOF even after truncation to prevent process pipe blockage
            }
            return sb.toString();
        } catch (Exception e) {
            return "";
        }
    }

    private void closeQuietly(Closeable c) {
        if (c != null) {
            try {
                c.close();
            } catch (IOException ignored) {}
        }
    }

    private void terminateProcess(Process process) {
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

    private void cleanUpMsvcObj(File workDir, File sourceFile) {
        if (workDir != null && sourceFile != null) {
            try {
                String baseName = sourceFile.getName().replaceAll("\\.[^.]+$", "");
                File objFile = new File(workDir, baseName + ".obj");
                if (objFile.exists()) {
                    if (!objFile.delete()) {
                        objFile.deleteOnExit();
                    }
                }
            } catch (Exception ignored) {}
        }
    }
}
