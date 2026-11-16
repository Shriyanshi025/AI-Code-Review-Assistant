package com.codereview.backend.analysis.performance;

import com.codereview.backend.analysis.core.AnalysisResult;
import com.codereview.backend.analysis.core.FindingCategory;
import com.codereview.backend.analysis.core.FindingDto;
import com.codereview.backend.analysis.core.FindingSeverity;
import com.codereview.backend.analysis.core.FindingSource;
import com.codereview.backend.entity.ProblemEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@Service
public class PerformanceAnalyzer {

    public PerformanceAnalysisResult analyze(AnalysisResult astResult, String code, ProblemEntity problem) {
        List<FindingDto> findings = new ArrayList<>();

        String timeComp = astResult.getEstimatedTimeComplexity() != null ? astResult.getEstimatedTimeComplexity() : "O(N)";
        String spaceComp = astResult.getEstimatedSpaceComplexity() != null ? astResult.getEstimatedSpaceComplexity() : "O(1)";

        // Rule 1: String Concatenation in Loop
        if (hasStringConcatInLoop(code)) {
            findings.add(FindingDto.builder()
                    .ruleId("JAVA_PERFORMANCE_STRING_CONCAT_IN_LOOP")
                    .severity(FindingSeverity.MEDIUM)
                    .category(FindingCategory.PERFORMANCE)
                    .title("Repeated String Concatenation in Loop")
                    .message("String concatenation using '+' or '+=' detected inside a loop body.")
                    .explanation("In Java, String objects are immutable. Appending strings with '+' inside a loop creates a new String instance on every iteration, leading to unnecessary object allocations and linear-copy overhead.")
                    .recommendation("Consider using StringBuilder or StringBuffer to accumulate string content inside loops efficiently.")
                    .hint("Why it matters: StringBuilder avoids intermediate object creation by maintaining a dynamic internal buffer.")
                    .confidence(0.9)
                    .source(FindingSource.DETERMINISTIC)
                    .build());
        }

        // Rule 2: Quadratic Time Complexity (Free-form or Problem-aware)
        boolean isQuadratic = "O(N^2)".equalsIgnoreCase(timeComp) || "O(N²)".equalsIgnoreCase(timeComp);
        if (isQuadratic) {
            if (problem == null) {
                findings.add(FindingDto.builder()
                        .ruleId("JAVA_PERFORMANCE_QUADRATIC_TIME")
                        .severity(FindingSeverity.MEDIUM)
                        .category(FindingCategory.PERFORMANCE)
                        .title("Potential Quadratic-Time Computation")
                        .message("Nested iteration suggests O(N²) time complexity.")
                        .explanation("Nested iteration over input-sized bounds causes the computation time to grow quadratically as input size increases.")
                        .recommendation("Consider whether repeated comparisons can be replaced with a lookup-based strategy (e.g., HashSet/HashMap) or a two-pointer pass if problem constraints allow.")
                        .hint("Why it matters: O(N²) algorithms may become slow or cause timeouts on large input datasets.")
                        .confidence(0.85)
                        .source(FindingSource.DETERMINISTIC)
                        .build());
            }
        }

        // Rule 3: Input-Proportional Space Allocation in Freeform Review
        if (problem == null && astResult.isHasInputSizeAllocation()) {
            spaceComp = "O(N)";
            findings.add(FindingDto.builder()
                    .ruleId("JAVA_PERFORMANCE_AUXILIARY_SPACE")
                    .severity(FindingSeverity.LOW)
                    .category(FindingCategory.PERFORMANCE)
                    .title("Input-Proportional Memory Allocation")
                    .message("An additional input-sized data structure is allocated.")
                    .explanation("Allocating an auxiliary array or collection proportional to input size increases memory usage to O(N).")
                    .recommendation("Consider whether in-place element swapping or single-pass traversal can achieve O(1) auxiliary space if memory efficiency is a priority.")
                    .hint("Why it matters: O(N) space creates additional heap garbage collection pressure on large inputs.")
                    .confidence(0.8)
                    .source(FindingSource.DETERMINISTIC)
                    .build());
        }

        boolean hasOptimization = !findings.isEmpty();

        return PerformanceAnalysisResult.builder()
                .timeComplexity(timeComp)
                .spaceComplexity(spaceComp)
                .hasOptimizationOpportunity(hasOptimization)
                .findings(findings)
                .build();
    }

    private boolean hasStringConcatInLoop(String code) {
        if (code == null) return false;

        // Check if string concat is present inside a loop
        // Regex looks for for/while loops containing string += or string + concatenation
        // Ignore if code already uses StringBuilder or StringBuffer for string accumulation
        boolean hasStringBuilder = code.contains("StringBuilder") || code.contains("StringBuffer");

        // Pattern for loop with string concatenation
        Pattern loopConcatPattern = Pattern.compile(
                "(for|while)\\s*\\([^\\)]*\\)\\s*\\{?[^}]*?([A-Za-z0-9_]+)\\s*\\+=\\s*[\"'][^\"']*[\"']|"+
                "(for|while)\\s*\\([^\\)]*\\)\\s*\\{?[^}]*?([A-Za-z0-9_]+)\\s*=\\s*\\2\\s*\\+\\s*",
                Pattern.DOTALL
        );

        if (loopConcatPattern.matcher(code).find()) {
            // Ensure loop isn't just using StringBuilder append
            return !hasStringBuilder;
        }

        // Additional check for String concatenation inside loop
        Pattern simpleStringLoopConcat = Pattern.compile(
                "(for|while)\\s*\\([^\\)]*\\)[^{]*\\{[^}]*?String\\s+[A-Za-z0-9_]+\\s*=\\s*\"\"[\\s\\S]*?\\+\\=|\n\\s*[A-Za-z0-9_]+\\s*\\+=\\s*[A-Za-z0-9_]+;"
        );
        if (simpleStringLoopConcat.matcher(code).find() && !hasStringBuilder) {
            return true;
        }

        if (code.contains("String") && (code.contains("+=") || code.contains("+")) && (code.contains("for") || code.contains("while"))) {
            // Check if string variable is declared outside loop and concatenated inside loop
            Pattern pattern = Pattern.compile("String\\s+([A-Za-z0-9_]+)\\s*=\\s*\"[^\"]*\";[\\s\\S]*?(for|while)\\s*\\([^\\)]*\\)[^{]*\\{[^}]*?\\1\\s*\\+=");
            if (pattern.matcher(code).find() && !hasStringBuilder) {
                return true;
            }
        }


        return false;
    }
}
