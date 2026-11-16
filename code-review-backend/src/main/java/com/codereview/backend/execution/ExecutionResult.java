package com.codereview.backend.execution;

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
public class ExecutionResult {

    private String status; // PASSED, FAILED, COMPILATION_ERROR, RUNTIME_ERROR, TIME_LIMIT_EXCEEDED, NOT_APPLICABLE, CLI_INPUT_REQUIRED
    private int passedTestCases;
    private int totalTestCases;
    private Long executionTimeMs;
    private Long memoryUsedKb;
    private String compilationOutput;
    private String userFacingSummary;

    @Builder.Default
    private boolean applicable = true;

    private String stdout;
    private String stderr;
    private boolean timedOut;
    private String applicabilityReason;

    @Builder.Default
    private List<TestCaseExecutionDetail> testCaseDetails = new ArrayList<>();
}

