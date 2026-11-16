package com.codereview.backend.service;

import com.codereview.backend.dto.BadgeDto;
import com.codereview.backend.dto.SubmissionHistoryDto;
import com.codereview.backend.entity.*;
import com.codereview.backend.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class BadgeSystemTest {

    @Autowired
    private BadgeService badgeService;

    @Autowired
    private BadgeSeeder badgeSeeder;

    @Autowired
    private SubmissionService submissionService;

    @Autowired
    private ProgressService progressService;

    @Autowired
    private ProblemService problemService;

    @Autowired
    private ReviewService reviewService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProblemRepository problemRepository;

    @Autowired
    private SubmissionRepository submissionRepository;

    @Autowired
    private UserBadgeRepository userBadgeRepository;

    @Autowired
    private ReviewSessionRepository reviewSessionRepository;

    private UserEntity userA;
    private UserEntity userB;

    @BeforeEach
    void setUp() {
        badgeSeeder.seedInitialBadges();

        userA = userRepository.save(UserEntity.builder()
                .username("test_user_a_" + System.currentTimeMillis())
                .email("usera_" + System.currentTimeMillis() + "@badge.com")
                .role("STUDENT")
                .build());

        userB = userRepository.save(UserEntity.builder()
                .username("test_user_b_" + System.currentTimeMillis())
                .email("userb_" + System.currentTimeMillis() + "@badge.com")
                .role("STUDENT")
                .build());
    }

    private ProblemEntity createProblem(String slug, String title, String category) {
        return problemRepository.save(ProblemEntity.builder()
                .title(title)
                .slug(slug + "_" + System.currentTimeMillis())
                .difficulty("EASY")
                .category(category)
                .description("Test problem description")
                .initialCode("public class Solution { }")
                .build());
    }

    private SubmissionEntity createDirectSubmission(UserEntity user, ProblemEntity problem, String status) {
        SubmissionEntity sub = submissionRepository.save(SubmissionEntity.builder()
                .user(user)
                .problem(problem)
                .code("class Solution {}")
                .language("JAVA")
                .status(status)
                .passedTestCases("PASSED".equalsIgnoreCase(status) ? 2 : 0)
                .totalTestCases(2)
                .executionTimeMs(10L)
                .memoryUsedKb(100L)
                .submittedAt(LocalDateTime.now())
                .build());

        // Create review session for accepted submission if passed
        ReviewSessionEntity rs = reviewSessionRepository.save(ReviewSessionEntity.builder()
                .user(user)
                .problem(problem)
                .submission(sub)
                .code("class Solution {}")
                .sessionType("PROBLEM_SUBMISSION")
                .overallScore("PASSED".equalsIgnoreCase(status) ? 95 : 60)
                .correctnessScore(95)
                .build());

        return sub;
    }

    @Test
    @DisplayName("1. New user has zero unlocked badges")
    void testNewUserHasZeroUnlockedBadges() {
        List<BadgeDto> unlocked = badgeService.getUnlockedBadges(userA.getId());
        assertTrue(unlocked.isEmpty(), "New user must start with zero unlocked badges");
    }

    @Test
    @DisplayName("2. First accepted challenge unlocks FIRST_STEP badge")
    void testFirstAcceptedChallengeUnlocksFirstStep() {
        ProblemEntity p1 = createProblem("prob-1", "Problem 1", "Arrays");
        createDirectSubmission(userA, p1, "PASSED");

        List<BadgeDto> newlyUnlocked = badgeService.evaluateBadges(userA);
        assertEquals(1, newlyUnlocked.size());
        assertEquals("FIRST_STEP", newlyUnlocked.get(0).getCode());

        List<BadgeDto> unlocked = badgeService.getUnlockedBadges(userA.getId());
        assertEquals(1, unlocked.size());
        assertEquals("FIRST_STEP", unlocked.get(0).getCode());
    }

    @Test
    @DisplayName("3. Four solved challenges unlocks FIRST_STEP only")
    void testFourSolvedChallenges() {
        for (int i = 1; i <= 4; i++) {
            ProblemEntity p = createProblem("prob-" + i, "Problem " + i, "Strings");
            createDirectSubmission(userA, p, "PASSED");
        }

        badgeService.evaluateBadges(userA);
        List<BadgeDto> unlocked = badgeService.getUnlockedBadges(userA.getId());
        assertEquals(1, unlocked.size());
        assertEquals("FIRST_STEP", unlocked.get(0).getCode());
    }

    @Test
    @DisplayName("4. Fifth solved challenge unlocks PROBLEM_SOLVER")
    void testFifthSolvedChallenge() {
        for (int i = 1; i <= 5; i++) {
            ProblemEntity p = createProblem("prob-" + i, "Problem " + i, "Strings");
            createDirectSubmission(userA, p, "PASSED");
        }

        badgeService.evaluateBadges(userA);
        List<BadgeDto> unlocked = badgeService.getUnlockedBadges(userA.getId());
        List<String> codes = unlocked.stream().map(BadgeDto::getCode).toList();
        assertTrue(codes.contains("FIRST_STEP"));
        assertTrue(codes.contains("PROBLEM_SOLVER"));
    }

    @Test
    @DisplayName("5. Tenth solved challenge unlocks RISING_CODER")
    void testTenthSolvedChallenge() {
        for (int i = 1; i <= 10; i++) {
            ProblemEntity p = createProblem("prob-" + i, "Problem " + i, "Strings");
            createDirectSubmission(userA, p, "PASSED");
        }

        badgeService.evaluateBadges(userA);
        List<BadgeDto> unlocked = badgeService.getUnlockedBadges(userA.getId());
        List<String> codes = unlocked.stream().map(BadgeDto::getCode).toList();
        assertTrue(codes.contains("FIRST_STEP"));
        assertTrue(codes.contains("PROBLEM_SOLVER"));
        assertTrue(codes.contains("RISING_CODER"));
    }

    @Test
    @DisplayName("6. Duplicate submissions for the same problem count as one solved problem")
    void testDuplicateSubmissionsSameProblem() {
        ProblemEntity p1 = createProblem("prob-1", "Problem 1", "Arrays");
        createDirectSubmission(userA, p1, "PASSED");
        createDirectSubmission(userA, p1, "PASSED");
        createDirectSubmission(userA, p1, "PASSED");

        badgeService.evaluateBadges(userA);
        List<BadgeDto> unlocked = badgeService.getUnlockedBadges(userA.getId());
        assertEquals(1, unlocked.size(), "Only FIRST_STEP should unlock for 1 unique solved problem");
    }

    @Test
    @DisplayName("7. Failed submission does not unlock solve-count badges")
    void testFailedSubmissionDoesNotUnlockBadges() {
        ProblemEntity p1 = createProblem("prob-1", "Problem 1", "Arrays");
        createDirectSubmission(userA, p1, "FAILED");

        badgeService.evaluateBadges(userA);
        List<BadgeDto> unlocked = badgeService.getUnlockedBadges(userA.getId());
        assertTrue(unlocked.isEmpty(), "Failed submission must not unlock solve badges");
    }

    @Test
    @DisplayName("8. First-Attempt Ace: accepted first submission counts")
    void testFirstAttemptAcePassFirstTry() {
        for (int i = 1; i <= 5; i++) {
            ProblemEntity p = createProblem("fa-prob-" + i, "FA Problem " + i, "Hashing");
            createDirectSubmission(userA, p, "PASSED");
        }

        badgeService.evaluateBadges(userA);
        List<BadgeDto> unlocked = badgeService.getUnlockedBadges(userA.getId());
        List<String> codes = unlocked.stream().map(BadgeDto::getCode).toList();
        assertTrue(codes.contains("FIRST_ATTEMPT_ACE"), "FIRST_ATTEMPT_ACE should unlock when 5 problems solved on first try");
    }

    @Test
    @DisplayName("9. First-Attempt Ace: failed then accepted does NOT count")
    void testFirstAttemptAceFailThenPassDoesNotCount() {
        for (int i = 1; i <= 5; i++) {
            ProblemEntity p = createProblem("fa-prob-" + i, "FA Problem " + i, "Hashing");
            // First submission FAILED
            createDirectSubmission(userA, p, "FAILED");
            // Second submission PASSED
            createDirectSubmission(userA, p, "PASSED");
        }

        badgeService.evaluateBadges(userA);
        List<BadgeDto> unlocked = badgeService.getUnlockedBadges(userA.getId());
        List<String> codes = unlocked.stream().map(BadgeDto::getCode).toList();
        assertFalse(codes.contains("FIRST_ATTEMPT_ACE"), "FIRST_ATTEMPT_ACE must NOT unlock if initial submissions failed");
    }

    @Test
    @DisplayName("10. Array Explorer: exactly five unique accepted Array problems unlock badge")
    void testArrayExplorer() {
        for (int i = 1; i <= 5; i++) {
            ProblemEntity p = createProblem("arr-prob-" + i, "Array Problem " + i, "Arrays");
            createDirectSubmission(userA, p, "PASSED");
        }

        badgeService.evaluateBadges(userA);
        List<BadgeDto> unlocked = badgeService.getUnlockedBadges(userA.getId());
        List<String> codes = unlocked.stream().map(BadgeDto::getCode).toList();
        assertTrue(codes.contains("ARRAY_EXPLORER"), "ARRAY_EXPLORER must unlock after 5 unique solved Array problems");
    }

    @Test
    @DisplayName("11. Topic Explorer: five distinct solved categories unlock badge")
    void testTopicExplorer() {
        String[] categories = {"Arrays", "Strings", "Sorting", "Searching", "Hashing"};
        for (int i = 0; i < 5; i++) {
            ProblemEntity p = createProblem("top-prob-" + i, "Topic Problem " + i, categories[i]);
            createDirectSubmission(userA, p, "PASSED");
        }

        badgeService.evaluateBadges(userA);
        List<BadgeDto> unlocked = badgeService.getUnlockedBadges(userA.getId());
        List<String> codes = unlocked.stream().map(BadgeDto::getCode).toList();
        assertTrue(codes.contains("TOPIC_EXPLORER"), "TOPIC_EXPLORER must unlock when 5 distinct categories are solved");
    }

    @Test
    @DisplayName("12. Quality Coder: fewer than five accepted submissions remains locked")
    void testQualityCoderUnderFiveSubmissions() {
        for (int i = 1; i <= 4; i++) {
            ProblemEntity p = createProblem("qc-prob-" + i, "QC Problem " + i, "Sorting");
            SubmissionEntity sub = submissionRepository.save(SubmissionEntity.builder()
                    .user(userA).problem(p).code("class S{}").language("JAVA").status("PASSED").passedTestCases(2).totalTestCases(2).submittedAt(LocalDateTime.now()).build());
            reviewSessionRepository.save(ReviewSessionEntity.builder()
                    .user(userA).problem(p).submission(sub).code("class S{}").sessionType("PROBLEM_SUBMISSION").overallScore(95).build());
        }

        badgeService.evaluateBadges(userA);
        List<BadgeDto> unlocked = badgeService.getUnlockedBadges(userA.getId());
        List<String> codes = unlocked.stream().map(BadgeDto::getCode).toList();
        assertFalse(codes.contains("QUALITY_CODER"), "QUALITY_CODER must remain locked with under 5 submissions");
    }

    @Test
    @DisplayName("13. Quality Coder: five accepted submissions with average >= 90 unlocks")
    void testQualityCoderFiveSubmissionsHighAvg() {
        for (int i = 1; i <= 5; i++) {
            ProblemEntity p = createProblem("qc-prob-" + i, "QC Problem " + i, "Sorting");
            SubmissionEntity sub = submissionRepository.save(SubmissionEntity.builder()
                    .user(userA).problem(p).code("class S{}").language("JAVA").status("PASSED").passedTestCases(2).totalTestCases(2).submittedAt(LocalDateTime.now()).build());
            reviewSessionRepository.save(ReviewSessionEntity.builder()
                    .user(userA).problem(p).submission(sub).code("class S{}").sessionType("PROBLEM_SUBMISSION").overallScore(92).build());
        }

        badgeService.evaluateBadges(userA);
        List<BadgeDto> unlocked = badgeService.getUnlockedBadges(userA.getId());
        List<String> codes = unlocked.stream().map(BadgeDto::getCode).toList();
        assertTrue(codes.contains("QUALITY_CODER"), "QUALITY_CODER must unlock with 5 accepted submissions and avg >= 90");
    }

    @Test
    @DisplayName("14. Quality Coder: free-form review scores must not count")
    void testQualityCoderFreeformIgnored() {
        // 5 freeform reviews with score 100
        for (int i = 1; i <= 5; i++) {
            reviewSessionRepository.save(ReviewSessionEntity.builder()
                    .user(userA).code("class Free{}").sessionType("FREEFORM_REVIEW").overallScore(100).build());
        }

        badgeService.evaluateBadges(userA);
        List<BadgeDto> unlocked = badgeService.getUnlockedBadges(userA.getId());
        List<String> codes = unlocked.stream().map(BadgeDto::getCode).toList();
        assertFalse(codes.contains("QUALITY_CODER"), "Freeform reviews must not count toward QUALITY_CODER badge");
    }

    @Test
    @DisplayName("15. Same badge cannot be unlocked twice")
    void testNoDuplicateUserBadge() {
        ProblemEntity p1 = createProblem("prob-1", "Problem 1", "Arrays");
        createDirectSubmission(userA, p1, "PASSED");

        List<BadgeDto> firstEval = badgeService.evaluateBadges(userA);
        assertEquals(1, firstEval.size());

        List<BadgeDto> secondEval = badgeService.evaluateBadges(userA);
        assertTrue(secondEval.isEmpty(), "Second evaluation should return zero newly unlocked badges");

        assertEquals(1, userBadgeRepository.findByUserId(userA.getId()).size());
    }

    @Test
    @DisplayName("16. User A's badges cannot appear for User B")
    void testUserBadgeIsolation() {
        ProblemEntity p1 = createProblem("prob-1", "Problem 1", "Arrays");
        createDirectSubmission(userA, p1, "PASSED");

        badgeService.evaluateBadges(userA);
        badgeService.evaluateBadges(userB);

        List<BadgeDto> userABadges = badgeService.getUnlockedBadges(userA.getId());
        List<BadgeDto> userBBadges = badgeService.getUnlockedBadges(userB.getId());

        assertEquals(1, userABadges.size());
        assertTrue(userBBadges.isEmpty(), "User B must have zero unlocked badges when only User A solved problems");
    }

    @Test
    @DisplayName("17. Existing persisted submissions can produce eligible badges")
    void testExistingPersistedSubmissionsEvaluate() {
        ProblemEntity p1 = createProblem("existing-1", "Existing 1", "Arrays");
        createDirectSubmission(userA, p1, "PASSED");

        // Now call getUserBadges which auto-evaluates
        List<BadgeDto> badges = badgeService.getUserBadges(userA.getId());
        BadgeDto firstStep = badges.stream().filter(b -> "FIRST_STEP".equals(b.getCode())).findFirst().orElseThrow();

        assertTrue(firstStep.isUnlocked(), "Pre-existing persisted submission should automatically grant badge when evaluated");
    }

    @Test
    @DisplayName("18. Badge evaluation after a submission does not corrupt Progress")
    void testBadgeEvaluationDoesNotCorruptProgress() {
        ProblemEntity p1 = createProblem("prob-1", "Problem 1", "Arrays");
        createDirectSubmission(userA, p1, "PASSED");

        UserProgressEntity progressBefore = progressService.getUserProgress(userA.getId());
        badgeService.evaluateBadges(userA);
        UserProgressEntity progressAfter = progressService.getUserProgress(userA.getId());

        assertEquals(progressBefore.getSolvedCount(), progressAfter.getSolvedCount());
        assertEquals(progressBefore.getAttemptedCount(), progressAfter.getAttemptedCount());
        assertEquals(progressBefore.getTotalSubmissions(), progressAfter.getTotalSubmissions());
    }

    @Test
    @DisplayName("19. Challenge status continues working")
    void testChallengeStatusContinuesWorking() {
        ProblemEntity p1 = createProblem("prob-1", "Problem 1", "Arrays");
        createDirectSubmission(userA, p1, "PASSED");
        badgeService.evaluateBadges(userA);

        ProblemEntity enriched = problemService.getProblemById(p1.getId(), userA.getId()).orElseThrow();
        assertEquals("SOLVED", enriched.getUserStatus());
    }

    @Test
    @DisplayName("20. Submission History continues working")
    void testSubmissionHistoryContinuesWorking() {
        ProblemEntity p1 = createProblem("prob-1", "Problem 1", "Arrays");
        createDirectSubmission(userA, p1, "PASSED");
        badgeService.evaluateBadges(userA);

        List<SubmissionHistoryDto> history = submissionService.getSubmissionHistoryByProblem(p1.getId(), userA.getId());
        assertEquals(1, history.size());
        assertEquals("PASSED", history.get(0).getStatus());
    }
}
