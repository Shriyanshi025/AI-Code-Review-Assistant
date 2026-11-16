package com.codereview.backend.analysis.performance;

import com.codereview.backend.analysis.core.FindingDto;
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
public class PerformanceAnalysisResult {

    private String timeComplexity;
    private String spaceComplexity;
    private boolean hasOptimizationOpportunity;

    @Builder.Default
    private List<FindingDto> findings = new ArrayList<>();
}
