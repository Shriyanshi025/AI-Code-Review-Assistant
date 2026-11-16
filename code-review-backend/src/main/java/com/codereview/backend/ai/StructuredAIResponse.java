package com.codereview.backend.ai;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StructuredAIResponse {

    private String summary;
    private String logicalReasoning;
    private String refactoringPlan;
    private String alternativeApproach;

    @Builder.Default
    private List<String> strengths = new ArrayList<>();

    @Builder.Default
    private List<String> edgeCaseSuggestions = new ArrayList<>();

    @Builder.Default
    private List<TestSuggestionItem> testSuggestions = new ArrayList<>();

    @Builder.Default
    private List<String> progressiveHints = new ArrayList<>();

    @Builder.Default
    private List<ContextualFindingItem> contextualFindings = new ArrayList<>();

    @Builder.Default
    private List<String> tradeoffs = new ArrayList<>();

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TestSuggestionItem {
        private String category; // NORMAL, BOUNDARY, EMPTY_NULL, DUPLICATE, NEGATIVE, MAX_CONSTRAINTS
        private String description;
        private String sampleInput;
        private String expectedBehavior;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ContextualFindingItem {
        private String title;
        private String severity; // LOW, MEDIUM, HIGH, CRITICAL
        private String explanation;
        private String whyItMatters;
        private String guidance;
    }
}
