package com.codereview.backend.review;

import com.codereview.backend.ai.DeterministicAIReviewAdapter;
import com.codereview.backend.analysis.java.JavaLanguageAnalyzer;
import com.codereview.backend.entity.FindingEntity;
import com.codereview.backend.entity.ReviewSessionEntity;
import com.codereview.backend.execution.ValidationExecutionEngine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.codereview.backend.service.StuckUserTracker;

import static org.junit.jupiter.api.Assertions.*;

class ReviewEngineTest {

    private ReviewEngine reviewEngine;
    private StuckUserTracker stuckUserTracker;

    @BeforeEach
    void setUp() {
        JavaLanguageAnalyzer javaAnalyzer = new JavaLanguageAnalyzer();
        ValidationExecutionEngine execEngine = new ValidationExecutionEngine(javaAnalyzer);
        DeterministicAIReviewAdapter aiAdapter = new DeterministicAIReviewAdapter();
        stuckUserTracker = new StuckUserTracker();

        reviewEngine = new ReviewEngine(javaAnalyzer, execEngine, aiAdapter, stuckUserTracker);
    }

    @Test
    @DisplayName("Should generate complete review session with scores, metrics, and findings")
    void testFreeformReviewGeneration() {
        String code = """
                public class Solution {
                    public int calculate(int[] arr) {
                        int max = 0;
                        for (int i = 0; i < arr.length; i++) {
                            if (arr[i] > max) max = arr[i];
                        }
                        return max;
                    }
                }
                """;

        ReviewSessionEntity session = reviewEngine.createReviewSession(null, null, null, code, "FREEFORM_REVIEW");

        assertNotNull(session);
        assertNotNull(session.getOverallScore());
        assertTrue(session.getOverallScore() > 0);
        assertNotNull(session.getTimeComplexity());
        assertNotNull(session.getSpaceComplexity());
        assertFalse(session.getFindings().isEmpty());
    }

    @Test
    @DisplayName("Verification Case 1 & 2: Inefficient solution gets 100 correctness but reduced overall score")
    void testInefficientNestedLoopSolution() {
        String code = """
                public class Solution {
                    public int findPair(int[] nums, int target) {
                        for (int i = 0; i < nums.length; i++) {
                            for (int j = 0; j < nums.length; j++) {
                                if (i != j && nums[i] + nums[j] == target) {
                                    return i;
                                }
                            }
                        }
                        return -1;
                    }
                }
                """;

        ReviewSessionEntity session = reviewEngine.createReviewSession(null, null, null, code, "FREEFORM_REVIEW");

        assertEquals(100, session.getCorrectnessScore(), "Syntax valid code should have 100 correctness");
        assertTrue(session.getPerformanceScore() < 100, "Nested loop should reduce performance score");
        assertTrue(session.getOverallScore() < 100, "Overall score must be less than 100 when performance/complexity are reduced!");
    }

    @Test
    @DisplayName("Verification Case 3: Solution with hardcoded secret has 100 correctness but reduced security & overall score")
    void testHardcodedSecretSolution() {
        String code = """
                public class Solution {
                    private String secretApiKey = "secret_key_12345";
                    public int process(int val) {
                        return val * 2;
                    }
                }
                """;

        ReviewSessionEntity session = reviewEngine.createReviewSession(null, null, null, code, "FREEFORM_REVIEW");

        assertEquals(100, session.getCorrectnessScore());
        assertTrue(session.getSecurityScore() < 100, "Hardcoded secret must reduce security score");
        assertTrue(session.getOverallScore() < 100, "Overall score must be reduced by security flaw even if correctness is 100");
    }

    @Test
    @DisplayName("Verification Case 4: Solution with empty catch block has reduced quality & overall score")
    void testEmptyCatchSolution() {
        String code = """
                public class Solution {
                    public int parse(String s) {
                        try {
                            return Integer.parseInt(s);
                        } catch (Exception e) {}
                        return 0;
                    }
                }
                """;

        ReviewSessionEntity session = reviewEngine.createReviewSession(null, null, null, code, "FREEFORM_REVIEW");

        assertEquals(100, session.getCorrectnessScore());
        assertTrue(session.getQualityScore() < 100, "Empty catch block must reduce quality score");
        assertTrue(session.getOverallScore() < 100);
    }

    @Test
    @DisplayName("Verification Case 5: Syntax compilation failure yields 0 correctness and low overall score")
    void testSyntaxCompilationFailure() {
        String invalidCode = "public class Solution { int x = ; }";

        ReviewSessionEntity session = reviewEngine.createReviewSession(null, null, null, invalidCode, "FREEFORM_REVIEW");

        assertEquals(0, session.getCorrectnessScore(), "Syntax error must result in 0 correctness");
        assertTrue(session.getOverallScore() < 40, "Compilation failure overall score must be low");
    }

    @Test
    @DisplayName("Verification Case 8: Mentor recommendations provide directional guidance without code spoilers")
    void testMentorSuggestionsNonSpoiling() {
        String code = """
                public class Solution {
                    public int findMax(int[] arr) {
                        int max = 0;
                        for (int i = 0; i < arr.length; i++) {
                            if (arr[i] > max) max = arr[i];
                        }
                        return max;
                    }
                }
                """;

        ReviewSessionEntity session = reviewEngine.createReviewSession(null, null, null, code, "FREEFORM_REVIEW");

        assertFalse(session.getFindings().isEmpty());
        for (FindingEntity finding : session.getFindings()) {
            assertNotNull(finding.getRecommendation());
            assertFalse(finding.getRecommendation().contains("Integer.MIN_VALUE"), "Suggestion must NOT spoil exact replacement code!");
            assertTrue(finding.getRecommendation().contains("Consider"), "Suggestion should provide mentor-style directional guidance!");
        }
    }

    @Test
    @DisplayName("Problem-aware: In-place requirement violation produces JAVA_INPLACE_REQUIRED_VIOLATION and reduces overall score")
    void testInPlaceRequirementViolation() {
        com.codereview.backend.entity.ProblemEntity problem = com.codereview.backend.entity.ProblemEntity.builder()
                .title("Reverse an Array")
                .expectedTimeComplexity("O(N)")
                .expectedSpaceComplexity("O(1)")
                .inPlaceRequired(true)
                .build();

        String codeWithAuxArray = """
                public class Solution {
                    public int[] reverseArray(int[] arr) {
                        int n = arr.length;
                        int[] b = new int[n];
                        for (int i = n - 1; i >= 0; i--) {
                            b[n - 1 - i] = arr[i];
                        }
                        return b;
                    }
                }
                """;

        ReviewSessionEntity session = reviewEngine.createReviewSession(null, problem, null, codeWithAuxArray, "CHALLENGE_SUBMISSION");

        assertEquals(100, session.getCorrectnessScore(), "Correctness remains 100 for valid syntax / passing code");
        assertTrue(session.getOverallScore() < 90, "Overall score must be reduced (e.g. < 90) when in-place O(1) requirement is violated");
        boolean hasInPlaceFinding = session.getFindings().stream()
                .anyMatch(f -> "JAVA_INPLACE_REQUIRED_VIOLATION".equals(f.getRuleId()));
        assertTrue(hasInPlaceFinding, "Finding JAVA_INPLACE_REQUIRED_VIOLATION must be generated");
    }

    @Test
    @DisplayName("Problem-aware: Optimal O(1) in-place solution achieves high overall score without violation finding")
    void testOptimalInPlaceSolution() {
        com.codereview.backend.entity.ProblemEntity problem = com.codereview.backend.entity.ProblemEntity.builder()
                .title("Reverse an Array")
                .expectedTimeComplexity("O(N)")
                .expectedSpaceComplexity("O(1)")
                .inPlaceRequired(true)
                .build();

        String inPlaceCode = """
                public class Solution {
                    public int[] reverseArray(int[] arr) {
                        int i = 0, j = arr.length - 1;
                        while (i < j) {
                            int temp = arr[i];
                            arr[i] = arr[j];
                            arr[j] = temp;
                            i++;
                            j--;
                        }
                        return arr;
                    }
                }
                """;

        ReviewSessionEntity session = reviewEngine.createReviewSession(null, problem, null, inPlaceCode, "CHALLENGE_SUBMISSION");

        assertEquals(100, session.getCorrectnessScore());
        assertTrue(session.getOverallScore() >= 90, "Optimal in-place solution should achieve high overall score (>= 90)");
        boolean hasInPlaceFinding = session.getFindings().stream()
                .anyMatch(f -> "JAVA_INPLACE_REQUIRED_VIOLATION".equals(f.getRuleId()));
        assertFalse(hasInPlaceFinding, "No in-place violation finding should be generated for optimal code");
    }

    @Test
    @DisplayName("Requirement 1 & 6: Free-form review generates actionable testing finding without penalizing correctness")
    void testFreeformReviewTestingFindingAndCorrectnessProtection() {
        String code = """
                public class Solution {
                    public int[] reverseArray(int[] arr) {
                        int left = 0, right = arr.length - 1;
                        while (left < right) {
                            int temp = arr[left];
                            arr[left] = arr[right];
                            arr[right] = temp;
                            left++; right--;
                        }
                        return arr;
                    }
                }
                """;

        ReviewSessionEntity session = reviewEngine.createReviewSession(null, null, null, code, "FREEFORM_REVIEW");

        assertEquals(100, session.getCorrectnessScore(), "Clean code correctness must NOT be penalized by lack of tests");
        assertEquals(50, session.getTestingScore(), "Freeform testing score should be 50 due to limited evidence");

        boolean hasTestingFinding = session.getFindings().stream()
                .anyMatch(f -> "TESTING".equals(f.getCategory()) && "JAVA_FREEFORM_TESTING_LIMITED".equals(f.getRuleId()));
        assertTrue(hasTestingFinding, "Finding of category TESTING must be generated for freeform review");

        FindingEntity testingFinding = session.getFindings().stream()
                .filter(f -> "JAVA_FREEFORM_TESTING_LIMITED".equals(f.getRuleId()))
                .findFirst().orElseThrow();

        assertTrue(testingFinding.getMessage().contains("Testing confidence is limited"), "Finding message must explain why testing score is lower");
        assertTrue(testingFinding.getRecommendation().contains("empty array"), "Finding recommendation must contain code-aware array suggestions");
    }

    @Test
    @DisplayName("Requirement 2 & 7: Code-aware suggestions generated for String input without array/tree noise")
    void testStringCodeAwareSuggestions() {
        String code = """
                public class Solution {
                    public boolean isPalindrome(String s) {
                        String clean = s.replaceAll("[^a-zA-Z0-9]", "").toLowerCase();
                        int i = 0, j = clean.length() - 1;
                        while (i < j) {
                            if (clean.charAt(i) != clean.charAt(j)) return false;
                            i++; j--;
                        }
                        return true;
                    }
                }
                """;

        ReviewSessionEntity session = reviewEngine.createReviewSession(null, null, null, code, "FREEFORM_REVIEW");

        FindingEntity testingFinding = session.getFindings().stream()
                .filter(f -> "JAVA_FREEFORM_TESTING_LIMITED".equals(f.getRuleId()))
                .findFirst().orElseThrow();

        String rec = testingFinding.getRecommendation();
        assertTrue(rec.contains("empty string"), "String code suggestions must mention empty string");
        assertTrue(rec.contains("single-character string"), "String code suggestions must mention single character string");
        assertFalse(rec.contains("empty array"), "String code suggestions must NOT contain array suggestions");
        assertFalse(rec.contains("skewed tree"), "String code suggestions must NOT contain tree suggestions");
    }

    @Test
    @DisplayName("Requirement 4: Challenge submission with all tests passing gets 100 testing score and no testing warning")
    void testChallengeSubmissionAllTestsPassing() {
        com.codereview.backend.entity.ProblemEntity problem = com.codereview.backend.entity.ProblemEntity.builder()
                .id(1L)
                .title("Two Sum")
                .testCases(java.util.List.of(
                        com.codereview.backend.entity.TestCaseEntity.builder().id(1L).input("nums = [2,7,11,15], target = 9").expectedOutput("[0, 1]").isHidden(false).build()
                ))
                .build();

        String code = """
                public class Solution {
                    public int[] twoSum(int[] nums, int target) {
                        for (int i = 0; i < nums.length; i++) {
                            for (int j = i + 1; j < nums.length; j++) {
                                if (nums[i] + nums[j] == target) return new int[]{i, j};
                            }
                        }
                        return new int[]{};
                    }
                }
                """;

        ReviewSessionEntity session = reviewEngine.createReviewSession(null, problem, null, code, "CHALLENGE_SUBMISSION");

        assertEquals(100, session.getCorrectnessScore());
        assertEquals(100, session.getTestingScore(), "Passing all challenge tests must yield 100 testing score");

        boolean hasTestingWarning = session.getFindings().stream()
                .anyMatch(f -> "TESTING".equals(f.getCategory()));
        assertFalse(hasTestingWarning, "Passing all challenge tests should NOT generate any warning testing finding");
    }

    @Test
    @DisplayName("Requirement 5: Challenge submission with partial test failure generates actionable testing finding")
    void testChallengeSubmissionPartialFailure() {
        com.codereview.backend.entity.ProblemEntity problem = com.codereview.backend.entity.ProblemEntity.builder()
                .id(1L)
                .title("Max Subarray")
                .testCases(java.util.List.of(
                        com.codereview.backend.entity.TestCaseEntity.builder().id(1L).input("nums = [1, 2, 3]").expectedOutput("6").isHidden(false).build(),
                        com.codereview.backend.entity.TestCaseEntity.builder().id(2L).input("nums = [-5, -2, -8]").expectedOutput("-2").isHidden(true).build()
                ))
                .build();

        String codeWithZeroInitBug = """
                public class Solution {
                    public int maxSubArray(int[] nums) {
                        int maxSoFar = 0;
                        int currentMax = 0;
                        for (int i = 0; i < nums.length; i++) {
                            currentMax += nums[i];
                            if (currentMax > maxSoFar) maxSoFar = currentMax;
                            if (currentMax < 0) currentMax = 0;
                        }
                        return maxSoFar;
                    }
                }
                """;

        ReviewSessionEntity session = reviewEngine.createReviewSession(null, problem, null, codeWithZeroInitBug, "CHALLENGE_SUBMISSION");

        assertEquals(50, session.getTestingScore(), "1 of 2 tests passed yields 50 testing score");

        boolean hasPartialFailFinding = session.getFindings().stream()
                .anyMatch(f -> "JAVA_CHALLENGE_TEST_SUITE_PARTIAL_FAILURE".equals(f.getRuleId()));
        assertTrue(hasPartialFailFinding, "Partial test failure finding must be generated");

        FindingEntity failFinding = session.getFindings().stream()
                .filter(f -> "JAVA_CHALLENGE_TEST_SUITE_PARTIAL_FAILURE".equals(f.getRuleId()))
                .findFirst().orElseThrow();

        assertTrue(failFinding.getMessage().contains("passed 1 out of 2"), "Finding message must indicate exact pass count");
    }

    @Test
    @DisplayName("Testing Evidence: Code with assertions improves testing score")
    void testCodeWithAssertionsImprovesTestingScore() {
        String codeWithoutAssert = """
                public class Solution {
                    public int square(int x) { return x * x; }
                }
                """;

        String codeWithAssert = """
                public class Solution {
                    public int square(int x) { return x * x; }
                    public static void main(String[] args) {
                        int res = new Solution().square(4);
                        assert res == 16;
                    }
                }
                """;

        ReviewSessionEntity s1 = reviewEngine.createReviewSession(null, null, null, codeWithoutAssert, "FREEFORM_REVIEW");
        ReviewSessionEntity s2 = reviewEngine.createReviewSession(null, null, null, codeWithAssert, "FREEFORM_REVIEW");

        assertTrue(s2.getTestingScore() > s1.getTestingScore(), "Adding assertions must improve testing score");
    }

    @Test
    @DisplayName("Testing Evidence: Comments containing test keywords do not count as executable testing evidence")
    void testCommentsDoNotCountAsTestingEvidence() {
        String codeWithComment = """
                public class Solution {
                    // test empty array
                    // test single element
                    public int sum(int[] arr) {
                        return 0;
                    }
                }
                """;

        ReviewSessionEntity session = reviewEngine.createReviewSession(null, null, null, codeWithComment, "FREEFORM_REVIEW");
        assertEquals(50, session.getTestingScore(), "Comments alone must NOT count as executable testing evidence");
    }

    @Test
    @DisplayName("Testing Evidence: Test harness without result validation gives limited credit")
    void testHarnessWithoutValidationGivesLimitedCredit() {
        String codeUnvalidated = """
                public class Solution {
                    public int findMax(int[] arr) { return 0; }
                    public static void main(String[] args) {
                        int[] arr = {1, 2, 3};
                        findMax(arr);
                    }
                }
                """;

        ReviewSessionEntity session = reviewEngine.createReviewSession(null, null, null, codeUnvalidated, "FREEFORM_REVIEW");
        assertTrue(session.getTestingScore() > 50 && session.getTestingScore() < 90, "Unvalidated harness calls receive limited credit");
        assertTrue(session.getFindings().stream().anyMatch(f -> "JAVA_FREEFORM_TESTING_LIMITED".equals(f.getRuleId())), "Unvalidated harness should still produce warning");
    }

    @Test
    @DisplayName("Testing Evidence: Strong testing evidence (assertions + multiple cases) clears testing warning")
    void testStrongTestingEvidenceClearsWarning() {
        String strongCode = """
                public class Solution {
                    public int findMax(int[] arr) {
                        if (arr == null || arr.length == 0) return 0;
                        int max = arr[0];
                        for (int x : arr) if (x > max) max = x;
                        return max;
                    }
                    public static void main(String[] args) {
                        int[] empty = {};
                        int[] single = {5};
                        int[] dups = {2, 2, 3};
                        int[] neg = {-5, -1};
                        assert findMax(empty) == 0;
                        assert findMax(single) == 5;
                        assert findMax(dups) == 3;
                        assert findMax(neg) == -1;
                    }
                }
                """;

        ReviewSessionEntity session = reviewEngine.createReviewSession(null, null, null, strongCode, "FREEFORM_REVIEW");
        assertTrue(session.getTestingScore() >= 90, "Strong testing evidence must yield score >= 90");
        boolean hasWarning = session.getFindings().stream().anyMatch(f -> "JAVA_FREEFORM_TESTING_LIMITED".equals(f.getRuleId()));
        assertFalse(hasWarning, "Testing warning must disappear when code contains strong testing evidence");
    }

    @Test
    @DisplayName("Stuck User Guidance: Progressive guidance triggers guided hint on attempt 3")
    void testProgressiveStuckGuidance() {
        String code = """
                public class Solution {
                    public int[] reverseArray(int[] arr) { return arr; }
                }
                """;

        // Attempt 1
        ReviewSessionEntity s1 = reviewEngine.createReviewSession(null, null, null, code, "FREEFORM_REVIEW");
        FindingEntity f1 = s1.getFindings().stream().filter(f -> "JAVA_FREEFORM_TESTING_LIMITED".equals(f.getRuleId())).findFirst().orElseThrow();
        assertFalse(f1.getHint().contains("// Guided Code Hint"));

        // Attempt 2 (code unchanged)
        ReviewSessionEntity s2 = reviewEngine.createReviewSession(null, null, null, code, "FREEFORM_REVIEW");
        FindingEntity f2 = s2.getFindings().stream().filter(f -> "JAVA_FREEFORM_TESTING_LIMITED".equals(f.getRuleId())).findFirst().orElseThrow();
        assertFalse(f2.getHint().contains("// Guided Code Hint"));

        // Attempt 3 (repeated code change / persistent attempt)
        // Simulate code modification that persits the same issue
        String codeMod = """
                public class Solution {
                    public int[] reverseArray(int[] arr) {
                        // minor edit
                        return arr;
                    }
                }
                """;
        ReviewSessionEntity s3 = reviewEngine.createReviewSession(null, null, null, codeMod, "FREEFORM_REVIEW");
        FindingEntity f3 = s3.getFindings().stream().filter(f -> "JAVA_FREEFORM_TESTING_LIMITED".equals(f.getRuleId())).findFirst().orElseThrow();
        assertTrue(f3.getHint().contains("// Guided Code Hint"), "Guided hint must appear on attempt 3+ for unresolved testing issue");
        assertFalse(f3.getHint().contains("left < right"), "Guided hint must NOT reveal the algorithm solution!");
    }

    @Test
    @DisplayName("Scoring Formula: Overall score is weighted calculation (30% Correctness, 20% Quality, 15% Complexity, 15% Maintainability, 10% Performance, 10% Testing)")
    void testWeightedOverallScoreCalculation() {
        String code = """
                public class Solution {
                    public int square(int x) { return x * x; }
                }
                """;

        ReviewSessionEntity session = reviewEngine.createReviewSession(null, null, null, code, "FREEFORM_REVIEW");
        int expected = (int) Math.round(
                (session.getCorrectnessScore() * 0.30) +
                (session.getQualityScore() * 0.20) +
                (session.getComplexityScore() * 0.15) +
                (session.getMaintainabilityScore() * 0.15) +
                (session.getPerformanceScore() * 0.10) +
                (session.getTestingScore() * 0.10)
        );
        expected = Math.max(0, Math.min(100, expected));

        assertEquals(expected, session.getOverallScore(), "Overall score must equal exact weighted formula");
    }

    @Test
    @DisplayName("Mandatory Acceptance Test: Scenario A (no test harness) vs Scenario B (strong assertions)")
    void testScenarioA_vs_ScenarioB() {
        String scenarioA = """
                public class Solution {
                    public int[] reverseArray(int[] arr) {
                        int left = 0;
                        int right = arr.length - 1;
                        while (left < right) {
                            int temp = arr[left];
                            arr[left] = arr[right];
                            arr[right] = temp;
                            left++;
                            right--;
                        }
                        return arr;
                    }
                }
                """;

        String scenarioB = """
                public class Solution {
                    public int[] reverseArray(int[] arr) {
                        int left = 0;
                        int right = arr.length - 1;
                        while (left < right) {
                            int temp = arr[left];
                            arr[left] = arr[right];
                            arr[right] = temp;
                            left++;
                            right--;
                        }
                        return arr;
                    }

                    public static void main(String[] args) {
                        int[] empty = {};
                        assert reverseArray(empty).length == 0;

                        int[] single = {5};
                        int[] singleResult = reverseArray(single);
                        assert singleResult.length == 1;
                        assert singleResult[0] == 5;

                        int[] duplicate = {1, 2, 2, 3};
                        int[] duplicateResult = reverseArray(duplicate);
                        assert duplicateResult[0] == 3;
                        assert duplicateResult[3] == 1;

                        int[] normal = {1, 2, 3, 4, 5};
                        int[] normalResult = reverseArray(normal);
                        assert normalResult[0] == 5;
                        assert normalResult[4] == 1;

                        int[] sorted = {1, 2, 3, 4};
                        int[] sortedResult = reverseArray(sorted);
                        assert sortedResult[0] == 4;

                        int[] large = new int[1000];
                        reverseArray(large);
                    }
                }
                """;

        ReviewSessionEntity resA = reviewEngine.createReviewSession(null, null, null, scenarioA, "FREEFORM_REVIEW");
        ReviewSessionEntity resB = reviewEngine.createReviewSession(null, null, null, scenarioB, "FREEFORM_REVIEW");

        // Assert Testing Score responds to evidence
        assertEquals(50, resA.getTestingScore(), "Scenario A without test harness should receive 50 testing score");
        assertEquals(100, resB.getTestingScore(), "Scenario B with strong assertions should receive 100 testing score");
        assertTrue(resB.getTestingScore() > resA.getTestingScore(), "Scenario B MUST receive a higher testing score than Scenario A");

        // Assert Findings respond to evidence
        boolean hasWarningA = resA.getFindings().stream().anyMatch(f -> "JAVA_FREEFORM_TESTING_LIMITED".equals(f.getRuleId()));
        boolean hasWarningB = resB.getFindings().stream().anyMatch(f -> "JAVA_FREEFORM_TESTING_LIMITED".equals(f.getRuleId()));

        assertTrue(hasWarningA, "Scenario A MUST produce JAVA_FREEFORM_TESTING_LIMITED warning finding");
        assertFalse(hasWarningB, "Scenario B MUST NOT produce JAVA_FREEFORM_TESTING_LIMITED warning finding");

        // Assert Overall Score responds to testing dimension
        assertTrue(resB.getOverallScore() > resA.getOverallScore(), "Scenario B overall score must be higher than Scenario A");
    }
}
