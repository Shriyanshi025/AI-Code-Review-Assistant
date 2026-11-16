package com.codereview.backend.analysis.core;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnalysisRequest {
    private String code;
    private String language;
    private Long problemId;
    private String problemDescription;
    private String problemConstraints;
    @Builder.Default
    private Map<String, Object> options = new HashMap<>();
}
