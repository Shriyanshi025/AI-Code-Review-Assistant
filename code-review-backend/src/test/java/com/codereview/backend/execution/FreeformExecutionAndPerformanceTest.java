package com.codereview.backend.execution;

import com.codereview.backend.ai.DeterministicAIReviewAdapter;
import com.codereview.backend.analysis.java.JavaLanguageAnalyzer;
import com.codereview.backend.entity.ReviewSessionEntity;
import com.codereview.backend.review.ReviewEngine;
import com.codereview.backend.service.StuckUserTracker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FreeformExecutionAndPerformanceTest {

    private JavaLanguageAnalyzer analyzer;
    private ValidationExecutionEngine execEngine;
    private ReviewEngine reviewEngine;

    @BeforeEach
    void setUp() {
        analyzer = new JavaLanguageAnalyzer();
        execEngine = new ValidationExecutionEngine(analyzer);
        DeterministicAIReviewAdapter aiAdapter = new DeterministicAIReviewAdapter();
        StuckUserTracker stuckTracker = new StuckUserTracker();

        reviewEngine = new ReviewEngine(analyzer, execEngine, aiAdapter, stuckTracker);
    }

    @Test
    @DisplayName("TEST 1: Hello World program executes successfully with stdout, no array suggestions, and no fake performance warning")
    void testHelloWorldExecution() {
        String code = """
                public class Hello {
                    public static void main(String[] args) {
                        System.out.println("Hello World ..!!");
                    }
                }
                """;

        ReviewSessionEntity session = reviewEngine.createReviewSession(null, null, null, code, "FREEFORM_REVIEW");

        assertNotNull(session);
        assertEquals("PASSED", session.getExecutionStatus());
        assertTrue(Boolean.TRUE.equals(session.getExecutionApplicable()));
        assertNotNull(session.getExecutionStdout());
        assertTrue(session.getExecutionStdout().contains("Hello World ..!!"));
        assertNotNull(session.getExecutionTimeMs());
        assertTrue(session.getExecutionTimeMs() > 0);

        // No array testing suggestions or false findings
        boolean hasArraySuggestion = session.getFindings().stream()
                .anyMatch(f -> f.getRecommendation() != null && f.getRecommendation().contains("Empty array"));
        assertFalse(hasArraySuggestion, "Hello World must NOT produce array testing suggestions");

        boolean hasStringConcatWarning = session.getFindings().stream()
                .anyMatch(f -> "JAVA_PERFORMANCE_STRING_CONCAT_IN_LOOP".equals(f.getRuleId()));
        assertFalse(hasStringConcatWarning, "Hello World must NOT produce string concat performance warning");
    }

    @Test
    @DisplayName("TEST 2: Reverse Array utility method detects array context, estimated complexity, and auxiliary space")
    void testReverseArrayPerformanceAndSpace() {
        String code = """
                public class Solution {
                    public int[] reverseArray(int[] arr) {
                        int[] copy = new int[arr.length];
                        for (int i = 0; i < arr.length; i++) {
                            copy[i] = arr[arr.length - 1 - i];
                        }
                        return copy;
                    }
                }
                """;

        ReviewSessionEntity session = reviewEngine.createReviewSession(null, null, null, code, "FREEFORM_REVIEW");

        assertNotNull(session);
        assertEquals("O(N)", session.getTimeComplexity());
        assertEquals("O(N)", session.getSpaceComplexity());

        boolean hasAuxSpaceFinding = session.getFindings().stream()
                .anyMatch(f -> "JAVA_PERFORMANCE_AUXILIARY_SPACE".equals(f.getRuleId()));
        assertTrue(hasAuxSpaceFinding, "Allocating new int[arr.length] in freeform review must produce JAVA_PERFORMANCE_AUXILIARY_SPACE finding!");
    }

    @Test
    @DisplayName("TEST 3: Nested Loop produces quadratic time complexity finding with non-judgmental explanation")
    void testNestedLoopQuadraticTime() {
        String code = """
                public class Solution {
                    public static void main(String[] args) {
                        int n = 100;
                        int count = 0;
                        for (int i = 0; i < n; i++) {
                            for (int j = 0; j < n; j++) {
                                count++;
                            }
                        }
                        System.out.println("Count: " + count);
                    }
                }
                """;

        ReviewSessionEntity session = reviewEngine.createReviewSession(null, null, null, code, "FREEFORM_REVIEW");

        assertEquals("PASSED", session.getExecutionStatus());
        assertEquals("O(N^2)", session.getTimeComplexity());

        boolean hasQuadraticFinding = session.getFindings().stream()
                .anyMatch(f -> "JAVA_PERFORMANCE_QUADRATIC_TIME".equals(f.getRuleId()));
        assertTrue(hasQuadraticFinding, "Nested loop must produce JAVA_PERFORMANCE_QUADRATIC_TIME finding");
    }

    @Test
    @DisplayName("TEST 4: Single Loop estimates O(N) time with no false quadratic optimization warning")
    void testSingleLoopNoFalseWarning() {
        String code = """
                public class Solution {
                    public static void main(String[] args) {
                        int sum = 0;
                        for (int i = 0; i < 100; i++) {
                            sum += i;
                        }
                        System.out.println(sum);
                    }
                }
                """;

        ReviewSessionEntity session = reviewEngine.createReviewSession(null, null, null, code, "FREEFORM_REVIEW");

        assertEquals("PASSED", session.getExecutionStatus());
        assertEquals("O(N)", session.getTimeComplexity());

        boolean hasQuadraticFinding = session.getFindings().stream()
                .anyMatch(f -> "JAVA_PERFORMANCE_QUADRATIC_TIME".equals(f.getRuleId()));
        assertFalse(hasQuadraticFinding, "Single loop must NOT produce quadratic time finding");
    }

    @Test
    @DisplayName("TEST 5: String Concatenation Loop produces string performance finding")
    void testStringConcatInLoop() {
        String code = """
                public class Solution {
                    public static void main(String[] args) {
                        String s = "";
                        for (int i = 0; i < 10; i++) {
                            s += "val" + i;
                        }
                        System.out.println(s);
                    }
                }
                """;

        ReviewSessionEntity session = reviewEngine.createReviewSession(null, null, null, code, "FREEFORM_REVIEW");

        boolean hasStringConcatFinding = session.getFindings().stream()
                .anyMatch(f -> "JAVA_PERFORMANCE_STRING_CONCAT_IN_LOOP".equals(f.getRuleId()));
        assertTrue(hasStringConcatFinding, "String concatenation with += inside loop must produce string performance finding");
    }

    @Test
    @DisplayName("TEST 6: StringBuilder inside Loop does NOT report string concatenation warning")
    void testStringBuilderInLoopNoWarning() {
        String code = """
                public class Solution {
                    public static void main(String[] args) {
                        StringBuilder sb = new StringBuilder();
                        for (int i = 0; i < 10; i++) {
                            sb.append("val").append(i);
                        }
                        System.out.println(sb.toString());
                    }
                }
                """;

        ReviewSessionEntity session = reviewEngine.createReviewSession(null, null, null, code, "FREEFORM_REVIEW");

        boolean hasStringConcatFinding = session.getFindings().stream()
                .anyMatch(f -> "JAVA_PERFORMANCE_STRING_CONCAT_IN_LOOP".equals(f.getRuleId()));
        assertFalse(hasStringConcatFinding, "StringBuilder usage in loop must NOT trigger string concat warning!");
    }

    @Test
    @DisplayName("TEST 7: Utility Method Without Main is marked NOT_APPLICABLE with no fake runtime")
    void testUtilityMethodWithoutMainNotApplicable() {
        String code = """
                public class MathUtils {
                    public int add(int a, int b) {
                        return a + b;
                    }
                }
                """;

        ReviewSessionEntity session = reviewEngine.createReviewSession(null, null, null, code, "FREEFORM_REVIEW");

        assertEquals("NOT_APPLICABLE", session.getExecutionStatus());
        assertFalse(Boolean.TRUE.equals(session.getExecutionApplicable()));
        assertNull(session.getExecutionTimeMs(), "Non-executable code must NOT manufacture a fake 0 ms runtime!");
        assertNotNull(session.getOverallScore());
    }

    @Test
    @DisplayName("TEST 8: Compilation Error reports COMPILATION_ERROR status and diagnostics while static review remains available")
    void testCompilationErrorHandling() {
        String code = """
                public class InvalidSyntax {
                    public static void main(String[] args) {
                        int x = ;
                    }
                }
                """;

        ReviewSessionEntity session = reviewEngine.createReviewSession(null, null, null, code, "FREEFORM_REVIEW");

        assertEquals("COMPILATION_ERROR", session.getExecutionStatus());
        assertTrue(session.getExecutionSummary().contains("Compilation Error"));
        assertNull(session.getExecutionTimeMs());
        assertNotNull(session.getFindings());
    }

    @Test
    @DisplayName("TEST 9: Runtime Exception reports RUNTIME_ERROR status and exception details")
    void testRuntimeExceptionHandling() {
        String code = """
                public class Buggy {
                    public static void main(String[] args) {
                        int[] arr = new int[2];
                        System.out.println(arr[5]);
                    }
                }
                """;

        ReviewSessionEntity session = reviewEngine.createReviewSession(null, null, null, code, "FREEFORM_REVIEW");

        assertEquals("RUNTIME_ERROR", session.getExecutionStatus());
        assertTrue(session.getExecutionStderr().contains("ArrayIndexOutOfBoundsException"));
        assertNotNull(session.getExecutionTimeMs());
    }

    @Test
    @DisplayName("TEST 10: Infinite loop times out cleanly with TIME_LIMIT_EXCEEDED")
    void testTimeoutHandling() {
        String code = """
                public class InfiniteLoop {
                    public static void main(String[] args) {
                        while (true) {
                            // endless loop
                        }
                    }
                }
                """;

        ExecutionResult result = execEngine.executeFreeform(code, 500L);

        assertEquals("TIME_LIMIT_EXCEEDED", result.getStatus());
        assertTrue(result.isTimedOut());
        assertEquals(500L, result.getExecutionTimeMs());
    }

    @Test
    @DisplayName("TEST 11: CLI Args in main method classified properly")
    void testCliArgsMain() {
        String code = """
                public class CLIProgram {
                    public static void main(String[] args) {
                        if (args.length == 0) {
                            System.out.println("No arguments passed");
                        } else {
                            System.out.println("Arg: " + args[0]);
                        }
                    }
                }
                """;

        ReviewSessionEntity session = reviewEngine.createReviewSession(null, null, null, code, "FREEFORM_REVIEW");

        assertEquals("PASSED", session.getExecutionStatus());
        assertTrue(session.getExecutionStdout().contains("No arguments passed"));
    }

    @Test
    @DisplayName("TEST 12: Already Efficient Code produces NO false optimization warnings")
    void testAlreadyEfficientCode() {
        String code = """
                public class FastSearch {
                    public static void main(String[] args) {
                        java.util.Set<Integer> set = new java.util.HashSet<>();
                        set.add(10);
                        set.add(20);
                        System.out.println(set.contains(10));
                    }
                }
                """;

        ReviewSessionEntity session = reviewEngine.createReviewSession(null, null, null, code, "FREEFORM_REVIEW");

        assertEquals("PASSED", session.getExecutionStatus());
        boolean hasPerfWarning = session.getFindings().stream()
                .anyMatch(f -> "JAVA_PERFORMANCE_QUADRATIC_TIME".equals(f.getRuleId()) || "JAVA_PERFORMANCE_STRING_CONCAT_IN_LOOP".equals(f.getRuleId()));
        assertFalse(hasPerfWarning, "Efficient code must NOT manufacture performance warnings!");
    }
}
