package com.codereview.backend.analysis.java;

import com.codereview.backend.analysis.core.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JavaLanguageAnalyzerTest {

    private JavaLanguageAnalyzer analyzer;

    @BeforeEach
    void setUp() {
        analyzer = new JavaLanguageAnalyzer();
    }

    @Test
    @DisplayName("Should detect zero-initialization pitfall for maximum tracking variable")
    void testZeroInitializationMaxPitfall() {
        String code = """
                public class Solution {
                    public int maxSubArray(int[] nums) {
                        int maxSoFar = 0;
                        int currentMax = 0;
                        for (int num : nums) {
                            currentMax += num;
                            if (currentMax > maxSoFar) {
                                maxSoFar = currentMax;
                            }
                        }
                        return maxSoFar;
                    }
                }
                """;

        AnalysisResult result = analyzer.analyze(AnalysisRequest.builder().code(code).language("JAVA").build());

        assertTrue(result.isSyntaxValid());
        assertNotNull(result.getFindings());
        boolean hasZeroInitFinding = result.getFindings().stream()
                .anyMatch(f -> "JAVA_ZERO_INIT_MAX_PITFALL".equals(f.getRuleId()));

        assertTrue(hasZeroInitFinding, "Analyzer should flag zero-initialization max value pitfall");
    }

    @Test
    @DisplayName("Should calculate cyclomatic complexity and nesting depth correctly")
    void testCyclomaticComplexityAndNesting() {
        String code = """
                public class Solution {
                    public void complexMethod(int n) {
                        if (n > 0) {
                            for (int i = 0; i < n; i++) {
                                if (i % 2 == 0) {
                                    System.out.println(i);
                                }
                            }
                        }
                    }
                }
                """;

        AnalysisResult result = analyzer.analyze(AnalysisRequest.builder().code(code).language("JAVA").build());

        assertTrue(result.isSyntaxValid());
        assertTrue(result.getCyclomaticComplexity() >= 3);
        assertEquals(3, result.getMaxNestingDepth());
        assertEquals("O(N)", result.getEstimatedTimeComplexity());
    }

    @Test
    @DisplayName("Should detect syntax errors and return line location")
    void testSyntaxErrorHandling() {
        String code = "public class Solution { public int invalid( { return 0; } }";

        AnalysisResult result = analyzer.analyze(AnalysisRequest.builder().code(code).language("JAVA").build());

        assertFalse(result.isSyntaxValid());
        assertNotNull(result.getParsingErrorMessage());
        assertFalse(result.getFindings().isEmpty());
        assertEquals("JAVA_SYNTAX_ERROR", result.getFindings().get(0).getRuleId());
    }
}
