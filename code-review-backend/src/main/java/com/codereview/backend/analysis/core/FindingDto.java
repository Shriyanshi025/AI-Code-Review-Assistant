package com.codereview.backend.analysis.core;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FindingDto {
    private String ruleId;
    private FindingSeverity severity;
    private FindingCategory category;
    private String title;
    private String message;
    private String explanation;
    private String recommendation;
    private String hint;
    private Integer lineStart;
    private Integer lineEnd;
    private Integer columnStart;
    private Integer columnEnd;
    private String codeSnippet;
    private Double confidence;
    private FindingSource source;
}
