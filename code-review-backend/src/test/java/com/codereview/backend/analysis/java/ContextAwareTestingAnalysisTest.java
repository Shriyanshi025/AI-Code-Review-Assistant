package com.codereview.backend.analysis.java;

import com.codereview.backend.analysis.core.AnalysisRequest;
import com.codereview.backend.analysis.core.AnalysisResult;
import com.codereview.backend.entity.ReviewSessionEntity;
import com.codereview.backend.execution.ValidationExecutionEngine;
import com.codereview.backend.review.ReviewEngine;
import com.codereview.backend.ai.DeterministicAIReviewAdapter;
import com.codereview.backend.service.StuckUserTracker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ContextAwareTestingAnalysisTest {

    private JavaLanguageAnalyzer analyzer;
    private ReviewEngine reviewEngine;

    @BeforeEach
    void setUp() {
        analyzer = new JavaLanguageAnalyzer();
        ValidationExecutionEngine execEngine = new ValidationExecutionEngine(analyzer);
        DeterministicAIReviewAdapter aiAdapter = new DeterministicAIReviewAdapter();
        StuckUserTracker stuckTracker = new StuckUserTracker();

        reviewEngine = new ReviewEngine(analyzer, execEngine, aiAdapter, stuckTracker);
    }

    @Test
    @DisplayName("TEST 1: Hello World program produces NO array edge cases, NO false boundary findings, and 100 testing score")
    void testHelloWorldNoArraySuggestions() {
        String code = """
                class Hello {
                    public static void main(String[] args) {
                        System.out.println("Hello World ..!!");
                    }
                }
                """;

        AnalysisResult result = analyzer.analyze(AnalysisRequest.builder().code(code).language("JAVA").build());

        assertFalse(result.isTestingApplicable(), "Hello World program must NOT be classified as testable parameterized logic");
        assertEquals("FIXED_OUTPUT", result.getTestingCategory());
        assertFalse(result.getDetectedInputTypes().contains("ARRAY"), "Unused String[] args must NOT trigger ARRAY input type!");
        assertTrue(result.getCodeAwareSuggestions().isEmpty(), "Hello World program must have NO array edge case suggestions!");

        ReviewSessionEntity session = reviewEngine.createReviewSession(null, null, null, code, "FREEFORM_REVIEW");
        assertEquals(100, session.getTestingScore(), "Hello World fixed output program must receive 100 testing score!");

        boolean hasTestingWarning = session.getFindings().stream()
                .anyMatch(f -> "JAVA_FREEFORM_TESTING_LIMITED".equals(f.getRuleId()));
        assertFalse(hasTestingWarning, "Hello World program must NOT produce JAVA_FREEFORM_TESTING_LIMITED warning finding!");
    }

    @Test
    @DisplayName("TEST 2: Reverse array algorithm detects ARRAY context and produces relevant array suggestions")
    void testReverseArrayDetectsArrayContext() {
        String code = """
                public class Solution {
                    public int[] reverseArray(int[] arr) {
                        for (int i = 0; i < arr.length / 2; i++) {
                            int temp = arr[i];
                            arr[i] = arr[arr.length - 1 - i];
                            arr[arr.length - 1 - i] = temp;
                        }
                        return arr;
                    }
                }
                """;

        AnalysisResult result = analyzer.analyze(AnalysisRequest.builder().code(code).language("JAVA").build());

        assertTrue(result.isTestingApplicable());
        assertEquals("TESTABLE_LOGIC", result.getTestingCategory());
        assertTrue(result.getDetectedInputTypes().contains("ARRAY"));
        assertFalse(result.getCodeAwareSuggestions().isEmpty(), "Array processing algorithm must provide array edge case suggestions!");
        assertTrue(result.getCodeAwareSuggestions().stream().anyMatch(s -> s.contains("Empty array")));
    }

    @Test
    @DisplayName("TEST 3: String processing algorithm produces string-specific suggestions and NO array suggestions")
    void testStringProcessingSuggestions() {
        String code = """
                public class Solution {
                    public String reverse(String s) {
                        return new StringBuilder(s).reverse().toString();
                    }
                }
                """;

        AnalysisResult result = analyzer.analyze(AnalysisRequest.builder().code(code).language("JAVA").build());

        assertTrue(result.isTestingApplicable());
        assertTrue(result.getDetectedInputTypes().contains("STRING"));
        assertFalse(result.getDetectedInputTypes().contains("ARRAY"));
        assertTrue(result.getCodeAwareSuggestions().stream().anyMatch(s -> s.contains("Empty string")));
        assertFalse(result.getCodeAwareSuggestions().stream().anyMatch(s -> s.contains("Empty array")), "String logic must NOT produce array suggestions!");
    }

    @Test
    @DisplayName("TEST 4: Numeric logic produces numeric suggestions and NO array suggestions")
    void testNumericLogicSuggestions() {
        String code = """
                public class Solution {
                    public int factorial(int n) {
                        if (n <= 1) return 1;
                        return n * factorial(n - 1);
                    }
                }
                """;

        AnalysisResult result = analyzer.analyze(AnalysisRequest.builder().code(code).language("JAVA").build());

        assertTrue(result.isTestingApplicable());
        assertTrue(result.getDetectedInputTypes().contains("NUMERIC"));
        assertFalse(result.getDetectedInputTypes().contains("ARRAY"));
        assertTrue(result.getCodeAwareSuggestions().stream().anyMatch(s -> s.contains("Zero (0)")));
        assertFalse(result.getCodeAwareSuggestions().stream().anyMatch(s -> s.contains("Empty array")));
    }

    @Test
    @DisplayName("TEST 5: Unused String[] args in main method does NOT cause array testing suggestions")
    void testUnusedMainArgsDoesNotTriggerArray() {
        String code = """
                public class Solution {
                    public static void main(String[] args) {
                        int x = 10;
                        System.out.println(x);
                    }
                }
                """;

        AnalysisResult result = analyzer.analyze(AnalysisRequest.builder().code(code).language("JAVA").build());

        assertFalse(result.getDetectedInputTypes().contains("ARRAY"));
        assertTrue(result.getCodeAwareSuggestions().isEmpty());
    }

    @Test
    @DisplayName("TEST 6: Actually using command-line arguments in main triggers CLI_ARGS context and NO array-algorithm suggestions")
    void testMainUsingArgsTriggersCliArgsContext() {
        String code = """
                public class Solution {
                    public static void main(String[] args) {
                        if (args.length == 0) {
                            System.out.println("No arguments");
                        } else {
                            System.out.println(args[0]);
                        }
                    }
                }
                """;

        AnalysisResult result = analyzer.analyze(AnalysisRequest.builder().code(code).language("JAVA").build());

        assertTrue(result.isTestingApplicable());
        assertEquals("CLI_ARGS", result.getTestingCategory());
        assertTrue(result.getDetectedInputTypes().contains("CLI_ARGS"));
        assertFalse(result.getDetectedInputTypes().contains("ARRAY"), "CLI arguments main must NOT be categorized as array algorithm!");
        assertTrue(result.getCodeAwareSuggestions().stream().anyMatch(s -> s.contains("No command-line arguments")));
        assertFalse(result.getCodeAwareSuggestions().stream().anyMatch(s -> s.contains("Sorted and reverse-sorted")), "CLI args must NOT suggest array sorting!");
    }
}
