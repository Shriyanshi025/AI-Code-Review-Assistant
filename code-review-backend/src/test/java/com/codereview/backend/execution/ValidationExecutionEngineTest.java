package com.codereview.backend.execution;

import com.codereview.backend.analysis.java.JavaLanguageAnalyzer;
import com.codereview.backend.entity.TestCaseEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ValidationExecutionEngineTest {

    private ValidationExecutionEngine engine;

    @BeforeEach
    void setUp() {
        JavaLanguageAnalyzer analyzer = new JavaLanguageAnalyzer();
        engine = new ValidationExecutionEngine(analyzer);
    }

    @Test
    @DisplayName("Should evaluate correct Java solution as PASSED")
    void testCorrectSolution() {
        String code = """
                public class Solution {
                    public int findLargest(int[] arr) {
                        int max = arr[0];
                        for (int val : arr) {
                            if (val > max) max = val;
                        }
                        return max;
                    }
                }
                """;

        List<TestCaseEntity> testCases = List.of(
                TestCaseEntity.builder().id(1L).input("[3, 7, 2, 9, 5]").expectedOutput("9").isHidden(false).build(),
                TestCaseEntity.builder().id(2L).input("[-5, -2, -8, -1]").expectedOutput("-1").isHidden(true).build()
        );

        ExecutionResult result = engine.execute(ExecutionRequest.builder()
                .code(code)
                .language("JAVA")
                .testCases(testCases)
                .build());

        assertEquals("PASSED", result.getStatus());
        assertEquals(2, result.getPassedTestCases());
        assertEquals(2, result.getTotalTestCases());

        // Confirm hidden test case is masked
        TestCaseExecutionDetail hiddenDetail = result.getTestCaseDetails().stream()
                .filter(TestCaseExecutionDetail::isHidden)
                .findFirst().orElseThrow();
        assertEquals("[Hidden Case]", hiddenDetail.getInput());
        assertEquals("[Hidden Case]", hiddenDetail.getExpectedOutput());
        assertEquals("[Hidden Case]", hiddenDetail.getActualOutput());
    }

    @Test
    @DisplayName("Should evaluate wrong Java solution as FAILED")
    void testWrongSolution() {
        String code = """
                public class Solution {
                    public int findLargest(int[] arr) {
                        return 0; // Always returns 0
                    }
                }
                """;

        List<TestCaseEntity> testCases = List.of(
                TestCaseEntity.builder().id(1L).input("[3, 7, 2, 9, 5]").expectedOutput("9").isHidden(false).build()
        );

        ExecutionResult result = engine.execute(ExecutionRequest.builder()
                .code(code)
                .language("JAVA")
                .testCases(testCases)
                .build());

        assertEquals("FAILED", result.getStatus());
        assertEquals(0, result.getPassedTestCases());
    }

    @Test
    @DisplayName("Should return COMPILATION_ERROR on syntax error")
    void testCompilationError() {
        String code = """
                public class Solution {
                    public int findLargest(int[] arr) {
                        return 0
                    }
                }
                """;

        ExecutionResult result = engine.execute(ExecutionRequest.builder()
                .code(code)
                .language("JAVA")
                .testCases(List.of())
                .build());

        assertEquals("COMPILATION_ERROR", result.getStatus());
    }
}
