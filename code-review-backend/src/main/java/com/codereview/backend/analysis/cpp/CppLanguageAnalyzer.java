package com.codereview.backend.analysis.cpp;

import com.codereview.backend.analysis.core.*;
import com.codereview.backend.execution.cpp.CppCompilerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Component
@RequiredArgsConstructor
public class CppLanguageAnalyzer implements LanguageAnalyzer {

    private final CppCompilerService compilerService;

    @Override
    public String getSupportedLanguage() {
        return "CPP";
    }

    public record SyntaxValidationResult(
            SyntaxStatus status,
            String errorMessage
    ) {
        public static SyntaxValidationResult valid() {
            return new SyntaxValidationResult(SyntaxStatus.VALID, null);
        }

        public static SyntaxValidationResult invalid(String errorMessage) {
            return new SyntaxValidationResult(SyntaxStatus.INVALID, errorMessage);
        }

        public static SyntaxValidationResult unavailable() {
            return new SyntaxValidationResult(SyntaxStatus.UNAVAILABLE, null);
        }
    }

    @Override
    public AnalysisResult analyze(AnalysisRequest request) {
        String code = request.getCode();
        if (code == null || code.trim().isEmpty()) {
            return AnalysisResult.builder()
                    .language("CPP")
                    .syntaxStatus(SyntaxStatus.INVALID)
                    .syntaxValid(false)
                    .parsingErrorMessage("Source code is empty")
                    .qualityScore(0)
                    .complexityScore(0)
                    .maintainabilityScore(0)
                    .securityScore(0)
                    .performanceScore(0)
                    .build();
        }

        // 1. Syntax Validation
        SyntaxValidationResult syntaxResult = validateSyntax(code);
        if (syntaxResult.status() == SyntaxStatus.INVALID) {
            List<FindingDto> syntaxFindings = new ArrayList<>();
            syntaxFindings.add(FindingDto.builder()
                    .ruleId("CPP_SYNTAX_ERROR")
                    .severity(FindingSeverity.HIGH)
                    .category(FindingCategory.CORRECTNESS)
                    .title("C++ Compilation / Syntax Error")
                    .message("The C++ source code contains syntax or compilation errors.")
                    .explanation(syntaxResult.errorMessage())
                    .recommendation("Ensure all includes, types, namespaces, and syntax adhere to valid C++.")
                    .lineStart(1)
                    .lineEnd(1)
                    .confidence(1.0)
                    .source(FindingSource.DETERMINISTIC)
                    .build());

            return AnalysisResult.builder()
                    .language("CPP")
                    .syntaxStatus(SyntaxStatus.INVALID)
                    .syntaxValid(false)
                    .parsingErrorMessage(syntaxResult.errorMessage())
                    .findings(syntaxFindings)
                    .qualityScore(20)
                    .complexityScore(50)
                    .maintainabilityScore(30)
                    .securityScore(90)
                    .performanceScore(50)
                    .build();
        }

        // 2. Structural & Metric Evaluation
        int cyclomaticComplexity = calculateCyclomaticComplexity(code);
        int maxNestingDepth = calculateMaxNestingDepth(code);
        String estimatedTime = estimateTimeComplexity(code);
        boolean hasAlloc = detectAuxiliaryAllocation(code);
        String estimatedSpace = hasAlloc ? "O(N)" : "O(1)";

        // 3. Static Rule Findings (C++ specific)
        List<FindingDto> findings = evaluateCppRules(code);

        // If compiler was unavailable, label syntax verification as unavailable
        if (syntaxResult.status() == SyntaxStatus.UNAVAILABLE) {
            findings.add(FindingDto.builder()
                    .ruleId("CPP_SYNTAX_CHECK_UNAVAILABLE")
                    .severity(FindingSeverity.LOW)
                    .category(FindingCategory.MAINTAINABILITY)
                    .title("Compiler Syntax Verification Unavailable")
                    .message("No C++ compiler was detected; compiler-based syntax validation was skipped.")
                    .explanation("Syntax verification is unavailable because no compatible C++ compiler (g++, clang++, or MSVC) was detected on the host system. Heuristic structural analysis has been performed, but full syntax validity cannot be guaranteed.")
                    .recommendation("Configure a supported C++ compiler (MSVC cl, g++, or clang++) to enable compiler-level syntax validation.")
                    .lineStart(1)
                    .lineEnd(1)
                    .confidence(1.0)
                    .source(FindingSource.DETERMINISTIC)
                    .build());
        }

        // 4. Sub-scores computation
        int highCount = (int) findings.stream().filter(f -> f.getSeverity() == FindingSeverity.HIGH).count();
        int medCount = (int) findings.stream().filter(f -> f.getSeverity() == FindingSeverity.MEDIUM).count();
        int lowCount = (int) findings.stream().filter(f -> f.getSeverity() == FindingSeverity.LOW).count();

        int complexityPenalty = Math.max(0, (cyclomaticComplexity - 4) * 5) + Math.max(0, (maxNestingDepth - 2) * 10);
        int complexityScore = Math.max(20, Math.min(100, 100 - complexityPenalty));

        int qualityScore = Math.max(30, Math.min(100, 100 - (highCount * 30 + medCount * 15 + lowCount * 5)));
        int maintainabilityScore = Math.max(30, Math.min(100, 100 - (maxNestingDepth > 3 ? 25 : 0) - (lowCount * 5)));
        int securityScore = Math.max(20, Math.min(100, 100 - (highCount * 40 + medCount * 15)));
        int performanceScore = estimatedTime.contains("O(N^2)") ? 55 : (estimatedTime.contains("O(N^3)") ? 35 : 95);
        if (hasAlloc) {
            performanceScore = Math.max(40, performanceScore - 10);
        }

        // 5. Code-aware testing suggestions
        List<String> detectedTypes = detectTypes(code);
        List<String> suggestions = generateTestingSuggestions(detectedTypes);

        Map<String, Object> metrics = new HashMap<>();
        metrics.put("CYCLOMATIC_COMPLEXITY", cyclomaticComplexity);
        metrics.put("MAX_NESTING_DEPTH", maxNestingDepth);
        metrics.put("LINE_COUNT", code.lines().count());
        metrics.put("SYNTAX_STATUS", syntaxResult.status().name());
        metrics.put("COMPILER_AVAILABLE", compilerService.isCompilerAvailable());

        boolean isValid = syntaxResult.status() == SyntaxStatus.VALID;
        return AnalysisResult.builder()
                .language("CPP")
                .syntaxStatus(syntaxResult.status())
                .syntaxValid(isValid ? Boolean.TRUE : null)
                .parsingErrorMessage(null)
                .findings(findings)
                .metrics(metrics)
                .cyclomaticComplexity(cyclomaticComplexity)
                .maxNestingDepth(maxNestingDepth)
                .estimatedTimeComplexity(estimatedTime)
                .estimatedSpaceComplexity(estimatedSpace)
                .hasInputSizeAllocation(hasAlloc)
                .qualityScore(qualityScore)
                .complexityScore(complexityScore)
                .maintainabilityScore(maintainabilityScore)
                .securityScore(securityScore)
                .performanceScore(performanceScore)
                .codeAwareSuggestions(suggestions)
                .detectedInputTypes(detectedTypes)
                .testingApplicable(true)
                .testingCategory("ALGORITHMIC")
                .evidenceTestingScore(75)
                .hasAssertions(code.contains("assert(") || code.contains("assert "))
                .build();
    }

    public SyntaxValidationResult validateSyntax(String code) {
        // Quick structural bracket check: unbalanced delimiters are a genuine syntax error
        if (!isBracketsBalanced(code)) {
            return SyntaxValidationResult.invalid(
                    "Syntax error: Unbalanced braces, parentheses, or brackets detected in C++ source.");
        }

        // If compiler is not available, syntax cannot be verified
        if (!compilerService.isCompilerAvailable()) {
            return SyntaxValidationResult.unavailable();
        }

        // Use compiler syntax check if compiler is present
        Path tempDir = null;
        try {
            tempDir = Files.createTempDirectory("cpp_syntax_");
            File src = new File(tempDir.toFile(), "syntax_check.cpp");
            Files.writeString(src.toPath(), code, StandardCharsets.UTF_8);

            CppCompilerService.CompilationResult result = compilerService.checkSyntax(src);
            if (!result.success()) {
                String errMsg = result.output() != null && !result.output().isEmpty()
                        ? result.output()
                        : "C++ syntax error during compilation check.";
                return SyntaxValidationResult.invalid(errMsg);
            }
            return SyntaxValidationResult.valid();
        } catch (Exception e) {
            log.warn("Syntax check error: {}", e.getMessage());
            return SyntaxValidationResult.unavailable();
        } finally {
            if (tempDir != null) {
                deleteDir(tempDir.toFile());
            }
        }
    }

    private boolean isBracketsBalanced(String code) {
        Deque<Character> stack = new ArrayDeque<>();
        boolean inLineComment = false;
        boolean inBlockComment = false;
        boolean inString = false;
        boolean inChar = false;

        for (int i = 0; i < code.length(); i++) {
            char c = code.charAt(i);
            char next = (i + 1 < code.length()) ? code.charAt(i + 1) : '\0';

            if (inLineComment) {
                if (c == '\n') inLineComment = false;
                continue;
            }
            if (inBlockComment) {
                if (c == '*' && next == '/') {
                    inBlockComment = false;
                    i++;
                }
                continue;
            }
            if (inString) {
                if (c == '\\') { i++; continue; }
                if (c == '"') inString = false;
                continue;
            }
            if (inChar) {
                if (c == '\\') { i++; continue; }
                if (c == '\'') inChar = false;
                continue;
            }

            if (c == '/' && next == '/') {
                inLineComment = true;
                i++;
                continue;
            }
            if (c == '/' && next == '*') {
                inBlockComment = true;
                i++;
                continue;
            }
            if (c == '"') { inString = true; continue; }
            if (c == '\'') { inChar = true; continue; }

            if (c == '{' || c == '(' || c == '[') {
                stack.push(c);
            } else if (c == '}' || c == ')' || c == ']') {
                if (stack.isEmpty()) return false;
                char top = stack.pop();
                if ((c == '}' && top != '{') ||
                    (c == ')' && top != '(') ||
                    (c == ']' && top != '[')) {
                    return false;
                }
            }
        }

        return stack.isEmpty() && !inBlockComment;
    }

    private int calculateCyclomaticComplexity(String code) {
        int complexity = 1;
        // Count branch points
        Pattern pattern = Pattern.compile("\\b(if|else\\s+if|for|while|case|catch)\\b|(&&|\\|\\||\\?)");
        Matcher m = pattern.matcher(code);
        while (m.find()) {
            complexity++;
        }
        return complexity;
    }

    private int calculateMaxNestingDepth(String code) {
        int maxDepth = 0;
        int currentDepth = 0;
        boolean inControl = false;

        for (int i = 0; i < code.length(); i++) {
            char c = code.charAt(i);
            if (c == '{') {
                currentDepth++;
                if (currentDepth > maxDepth) {
                    maxDepth = currentDepth;
                }
            } else if (c == '}') {
                if (currentDepth > 0) currentDepth--;
            }
        }
        return Math.max(1, maxDepth);
    }

    private String estimateTimeComplexity(String code) {
        int loopNesting = 0;
        int maxLoopNesting = 0;

        String[] lines = code.split("\n");
        for (String line : lines) {
            String trimmed = line.trim();
            if (trimmed.startsWith("for") || trimmed.startsWith("while") || trimmed.contains(" for (") || trimmed.contains(" while (")) {
                loopNesting++;
                if (loopNesting > maxLoopNesting) {
                    maxLoopNesting = loopNesting;
                }
            }
            if (trimmed.contains("}")) {
                loopNesting = Math.max(0, loopNesting - 1);
            }
        }

        if (maxLoopNesting >= 3) return "O(N^3)";
        if (maxLoopNesting == 2) return "O(N^2)";
        if (maxLoopNesting == 1) return "O(N)";
        return "O(1)";
    }

    private boolean detectAuxiliaryAllocation(String code) {
        // Detects vector creation, new[] allocation, or large auxiliary structures
        return code.contains("vector<") || code.contains("new int[") || code.contains("new char[") || code.contains("new double[");
    }

    private List<FindingDto> evaluateCppRules(String code) {
        List<FindingDto> findings = new ArrayList<>();
        String[] lines = code.split("\n");

        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            int lineNum = i + 1;

            // 1. Insecure C functions (gets, strcpy, sprintf)
            if (line.matches(".*\\bgets\\s*\\(.*")) {
                findings.add(FindingDto.builder()
                        .ruleId("CPP_INSECURE_GETS")
                        .severity(FindingSeverity.HIGH)
                        .category(FindingCategory.SECURITY)
                        .title("Dangerous Function Usage: 'gets'")
                        .message("The 'gets()' function does not perform bounds checking and is inherently vulnerable to buffer overflows.")
                        .explanation("'gets()' was deprecated in C++11 and removed in C++14 because it allows writing past buffer boundaries.")
                        .recommendation("Use std::getline(std::cin, str) or std::cin >> str for safe string input.")
                        .lineStart(lineNum)
                        .lineEnd(lineNum)
                        .codeSnippet(line.trim())
                        .confidence(1.0)
                        .source(FindingSource.DETERMINISTIC)
                        .build());
            }

            if (line.matches(".*\\bstrcpy\\s*\\(.*") || line.matches(".*\\bstrcat\\s*\\(.*")) {
                findings.add(FindingDto.builder()
                        .ruleId("CPP_UNCHECKED_STRING_COPY")
                        .severity(FindingSeverity.MEDIUM)
                        .category(FindingCategory.SECURITY)
                        .title("Potential Buffer Overflow: Unbounded C String Operation")
                        .message("C-style unbounded string copy/concatenation can overwrite adjacent memory if destination buffer is too small.")
                        .explanation("Functions like 'strcpy' and 'strcat' do not verify destination buffer capacity.")
                        .recommendation("Use std::string assignment (=, +=) or bounds-checked functions (strncpy, snprintf).")
                        .lineStart(lineNum)
                        .lineEnd(lineNum)
                        .codeSnippet(line.trim())
                        .confidence(0.9)
                        .source(FindingSource.DETERMINISTIC)
                        .build());
            }

            // 2. Vector pass by value in function signatures
            if (line.matches(".*\\b(void|int|bool|double|string|auto)\\s+[a-zA-Z0-9_]+\\s*\\([^)]*\\bvector<[^>]+>\\s+[a-zA-Z0-9_]+[^&]*\\).*")) {
                findings.add(FindingDto.builder()
                        .ruleId("CPP_CONTAINER_PASS_BY_VALUE")
                        .severity(FindingSeverity.LOW)
                        .category(FindingCategory.PERFORMANCE)
                        .title("std::vector Passed by Value")
                        .message("Passing std::vector by value triggers a deep copy of all elements upon every function call.")
                        .explanation("Deep copying large containers adds O(N) time and memory overhead for each invocation.")
                        .recommendation("Pass by const reference ('const std::vector<T>&') if read-only, or by non-const reference ('std::vector<T>&') if in-place modification is required.")
                        .lineStart(lineNum)
                        .lineEnd(lineNum)
                        .codeSnippet(line.trim())
                        .confidence(0.85)
                        .source(FindingSource.DETERMINISTIC)
                        .build());
            }

            // 3. Raw pointer allocation without smart pointer
            if (line.contains("new ") && !code.contains("delete ") && !code.contains("delete[] ")) {
                findings.add(FindingDto.builder()
                        .ruleId("CPP_RAW_POINTER_POTENTIAL_LEAK")
                        .severity(FindingSeverity.MEDIUM)
                        .category(FindingCategory.CODE_QUALITY)
                        .title("Manual Memory Allocation Without Matching 'delete'")
                        .message("Detected dynamic memory allocation with 'new', but no corresponding 'delete' statement was found.")
                        .explanation("Failing to free dynamically allocated memory leads to memory leaks across long-running processes.")
                        .recommendation("Prefer modern C++ smart pointers (std::unique_ptr, std::shared_ptr) or RAII containers (std::vector) to manage lifetimes automatically.")
                        .lineStart(lineNum)
                        .lineEnd(lineNum)
                        .codeSnippet(line.trim())
                        .confidence(0.85)
                        .source(FindingSource.DETERMINISTIC)
                        .build());
            }
        }

        return findings;
    }

    private List<String> detectTypes(String code) {
        List<String> types = new ArrayList<>();
        if (code.contains("vector") || code.contains("[]") || code.contains("arr")) {
            types.add("ARRAY");
        }
        if (code.contains("string") || code.contains("char*") || code.contains("char ")) {
            types.add("STRING");
        }
        if (code.contains("int ") || code.contains("long ") || code.contains("double ")) {
            types.add("NUMERIC");
        }
        return types;
    }

    private List<String> generateTestingSuggestions(List<String> types) {
        List<String> suggestions = new ArrayList<>();
        if (types.contains("ARRAY")) {
            suggestions.add("• Empty container ([])");
            suggestions.add("• Single-element vector/array");
            suggestions.add("• Vector with duplicate values");
        }
        if (types.contains("STRING")) {
            suggestions.add("• Empty string (\"\")");
            suggestions.add("• Single-character string");
            suggestions.add("• Strings containing spaces or special characters");
        }
        if (types.contains("NUMERIC")) {
            suggestions.add("• Zero input (0)");
            suggestions.add("• Negative integer boundary values");
            suggestions.add("• Maximum constraint limits (INT_MAX, INT_MIN)");
        }
        if (suggestions.isEmpty()) {
            suggestions.add("• Parameter boundary validation");
            suggestions.add("• Edge input handling");
        }
        return suggestions;
    }

    private void deleteDir(File dir) {
        File[] files = dir.listFiles();
        if (files != null) {
            for (File f : files) {
                if (f.isDirectory()) deleteDir(f);
                else f.delete();
            }
        }
        dir.delete();
    }
}
