package com.codereview.backend.analysis.core;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnalysisResult {

    private String language;
    private boolean syntaxValid;
    private String parsingErrorMessage;

    @Builder.Default
    private List<FindingDto> findings = new ArrayList<>();

    @Builder.Default
    private Map<String, Object> metrics = new HashMap<>();

    private Integer cyclomaticComplexity;
    private Integer maxNestingDepth;
    private String estimatedTimeComplexity;
    private String estimatedSpaceComplexity;
    private boolean hasInputSizeAllocation;

    private Integer qualityScore;
    private Integer complexityScore;
    private Integer maintainabilityScore;
    private Integer securityScore;
    private Integer performanceScore;

    @Builder.Default
    private List<String> codeAwareSuggestions = new ArrayList<>();

    @Builder.Default
    private List<String> detectedInputTypes = new ArrayList<>();

    @Builder.Default
    private List<String> coveredCases = new ArrayList<>();

    @Builder.Default
    private List<String> missingCases = new ArrayList<>();

    private Integer evidenceTestingScore;
    private boolean hasAssertions;

    @Builder.Default
    private boolean testingApplicable = true;

    private String testingCategory;
}
