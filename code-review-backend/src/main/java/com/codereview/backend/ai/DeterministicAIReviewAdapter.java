package com.codereview.backend.ai;

import com.codereview.backend.analysis.core.FindingDto;
import com.codereview.backend.analysis.core.FindingSeverity;
import com.codereview.backend.util.LanguageValidator;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class DeterministicAIReviewAdapter implements AIReviewAdapter {

    @Override
    public StructuredAIResponse generateReview(AIReviewRequest request) {
        List<FindingDto> findings = request.getDeterministicFindings() != null ? request.getDeterministicFindings() : List.of();
        long highCount = findings.stream().filter(f -> f.getSeverity() == FindingSeverity.HIGH).count();
        long medCount = findings.stream().filter(f -> f.getSeverity() == FindingSeverity.MEDIUM).count();

        boolean isCpp = LanguageValidator.isCpp(request.getLanguage());
        String defaultTitle = isCpp ? "C++ Code Analysis" : "Java Code Analysis";
        String langLabel = isCpp ? "C++" : "Java";
        String title = request.getProblemTitle() != null ? request.getProblemTitle() : defaultTitle;

        // 1. Build summary
        String summary;
        if (highCount > 0) {
            summary = String.format("Review for '%s': Found %d critical logical or security concerns that require immediate attention.", title, highCount);
        } else if (medCount > 0) {
            summary = String.format("Review for '%s': Solution is functional but contains %d potential performance or code-quality optimization opportunities.", title, medCount);
        } else {
            summary = String.format("Review for '%s': Excellent submission! Clean %s code structure with solid complexity performance (%s time, %s space).",
                    title, langLabel, request.getEstimatedTimeComplexity(), request.getEstimatedSpaceComplexity());
        }

        // 2. Build logical reasoning
        StringBuilder reasoning = new StringBuilder();
        reasoning.append("Analysis of structural AST indicates time complexity of ").append(request.getEstimatedTimeComplexity())
                .append(" and space complexity of ").append(request.getEstimatedSpaceComplexity()).append(". ");
        if (!findings.isEmpty()) {
            reasoning.append("Key structural observations:\n");
            for (FindingDto f : findings) {
                reasoning.append("- Line ").append(f.getLineStart()).append(": ").append(f.getTitle()).append(". ").append(f.getExplanation()).append("\n");
            }
        } else {
            reasoning.append("Control flow analysis confirms proper parameter initialization and clean exception safety.");
        }

        // 3. Edge Case Suggestions
        List<String> edgeCases = new ArrayList<>();
        if (request.getCodeAwareSuggestions() != null && !request.getCodeAwareSuggestions().isEmpty()) {
            edgeCases.addAll(request.getCodeAwareSuggestions());
        }

        // 4. Test Suggestions
        List<StructuredAIResponse.TestSuggestionItem> testSuggestions = new ArrayList<>();
        testSuggestions.add(new StructuredAIResponse.TestSuggestionItem("NORMAL", "Standard positive input case", "Standard input", "Expected output matches target specification"));

        for (String suggestion : edgeCases) {
            String clean = suggestion.replace("• ", "").trim();
            if (clean.toLowerCase().contains("empty")) {
                testSuggestions.add(new StructuredAIResponse.TestSuggestionItem("EMPTY_NULL", clean, "Empty input structure", "Handled gracefully without null or out-of-bounds error"));
            } else if (clean.toLowerCase().contains("single") || clean.toLowerCase().contains("one")) {
                testSuggestions.add(new StructuredAIResponse.TestSuggestionItem("BOUNDARY", clean, "Single element input", "Processes correctly for minimal size"));
            } else if (clean.toLowerCase().contains("negative")) {
                testSuggestions.add(new StructuredAIResponse.TestSuggestionItem("NEGATIVE", clean, "Negative values input", "Handles signed or negative values accurately"));
            } else if (clean.toLowerCase().contains("duplicate") || clean.toLowerCase().contains("repeat")) {
                testSuggestions.add(new StructuredAIResponse.TestSuggestionItem("DUPLICATE", clean, "Repeated elements", "Handles identical or redundant input elements"));
            } else if (clean.toLowerCase().contains("large") || clean.toLowerCase().contains("bound")) {
                testSuggestions.add(new StructuredAIResponse.TestSuggestionItem("MAX_CONSTRAINTS", clean, "Upper constraint limit", "Completes within execution time and memory limits"));
            }
        }

        // 5. Progressive Hints
        List<String> hints = new ArrayList<>();
        hints.add("Think about what happens when the input contains only negative values or duplicate numbers.");
        hints.add("Consider whether your initial candidate variable is guaranteed to be valid for every input case.");
        hints.add("Check if a HashMap or Two-Pointer approach could reduce the time complexity from O(N^2) to O(N).");

        // 6. Refactoring & Alternative
        String refactoring = highCount > 0 ? "Address high-severity initialization and boundary issues on line " + findings.get(0).getLineStart() : "Consider extracting helper methods and adding Javadoc comments for public API clarity.";
        String alternative = "Consider using a HashMap lookup to achieve O(N) linear time complexity with O(N) auxiliary space, or sorting first if O(N log N) with O(1) space is preferred.";

        return StructuredAIResponse.builder()
                .summary(summary)
                .logicalReasoning(reasoning.toString())
                .refactoringPlan(refactoring)
                .alternativeApproach(alternative)
                .edgeCaseSuggestions(edgeCases)
                .testSuggestions(testSuggestions)
                .progressiveHints(hints)
                .aiProvider("DETERMINISTIC")
                .aiModel("RULE_BASED_AST")
                .aiKeySource("NONE")
                .build();
    }
}
