package com.codereview.backend.execution;

import com.codereview.backend.entity.TestCaseEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExecutionRequest {
    private String code;
    private String language;
    private List<TestCaseEntity> testCases;
    @Builder.Default
    private long timeLimitMs = 2000;
    @Builder.Default
    private long memoryLimitMb = 256;
}
