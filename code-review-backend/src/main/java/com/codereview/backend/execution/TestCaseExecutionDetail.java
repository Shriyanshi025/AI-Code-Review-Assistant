package com.codereview.backend.execution;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TestCaseExecutionDetail {
    private Long testCaseId;
    private boolean passed;
    private String input;
    private String expectedOutput;
    private String actualOutput;
    private String failureReason;
    private boolean isHidden;
}
