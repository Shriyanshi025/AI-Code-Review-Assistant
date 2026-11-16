package com.codereview.backend.execution;

public interface ExecutionEngine {

    /**
     * Executes untrusted user code against target problem test cases using an isolated sandbox contract.
     *
     * @param request the execution request containing source code and test cases
     * @return structured execution result
     */
    ExecutionResult execute(ExecutionRequest request);
}
