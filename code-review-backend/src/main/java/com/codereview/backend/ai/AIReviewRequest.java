package com.codereview.backend.ai;

import com.codereview.backend.analysis.core.FindingDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AIReviewRequest {
    private String code;
    private String language;
    private List<FindingDto> deterministicFindings;
    private Map<String, Object> metrics;
    private String estimatedTimeComplexity;
    private String estimatedSpaceComplexity;
    private String problemTitle;
    private String problemDescription;
    private String problemConstraints;
    private List<String> codeAwareSuggestions;
    private String executionSummary;
    private Boolean isChallengeMode;

    private String executionStatus;
    private String executionStdout;
    private String executionStderr;
    private Long executionTimeMs;
    private Boolean executionApplicable;
}

