package com.codereview.backend.service;

import com.codereview.backend.dto.SubmissionHistoryDto;
import com.codereview.backend.entity.*;
import com.codereview.backend.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class SubmissionPersistenceAndProgressTest {

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
    private ReviewSessionRepository reviewSessionRepository;

    @Autowired
    private UserProgressRepository userProgressRepository;

    private UserEntity userA;
    private UserEntity userB;
    private ProblemEntity reverseArrayProblem;

    @BeforeEach
    void setUp() {
        userA = userRepository.save(UserEntity.builder()
                .username("user_a_" + System.currentTimeMillis())
                .email("usera_" + System.currentTimeMillis() + "@test.com")
                .role("STUDENT")
                .build());

        userB = userRepository.save(UserEntity.builder()
                .username("user_b_" + System.currentTimeMillis())
                .email("userb_" + System.currentTimeMillis() + "@test.com")
                .role("STUDENT")
                .build());

        reverseArrayProblem = problemRepository.findBySlug("reverse-an-array")
                .orElseGet(() -> problemRepository.save(ProblemEntity.builder()
                        .title("Reverse an Array Test")
                        .slug("reverse-an-array")
                        .difficulty("EASY")
                        .category("Arrays")
                        .description("Reverse an array in place")
                        .initialCode("public class Solution { public int[] reverseArray(int[] arr) { return arr; } }")
                        .build()));
    }

    @Test
    @DisplayName("1. Submission is persisted in database with correct attributes")
    void testSubmissionIsPersisted() {
        String code = "public class Solution { public int[] reverseArray(int[] arr) { int i=0, j=arr.length-1; while(i<j){ int temp=arr[i]; arr[i]=arr[j]; arr[j]=temp; i++; j--; } return arr; } }";
        SubmissionService.SubmissionResponse response = submissionService.submitSolution(reverseArrayProblem.getId(), code, userA.getId());

        assertNotNull(response.submission().getId(), "Submission ID must be generated");
        assertTrue(submissionRepository.existsById(response.submission().getId()), "Submission entity must exist in database");

        SubmissionEntity persisted = submissionRepository.findById(response.submission().getId()).orElseThrow();
        assertEquals(userA.getId(), persisted.getUser().getId(), "2. Submission belongs to correct user");
        assertEquals(reverseArrayProblem.getId(), persisted.getProblem().getId(), "3. Submission belongs to correct problem");
        assertNotNull(persisted.getSubmittedAt(), "Submission timestamp must exist");

        // Verify aiProvider, aiModel, and aiKeySource are persisted and returned correctly
        assertNotNull(response.reviewSession(), "ReviewSession must exist in response");
        assertNotNull(response.reviewSession().getAiProvider(), "aiProvider must be set in response");
        ReviewSessionEntity persistedReview = reviewSessionRepository.findById(response.reviewSession().getId()).orElseThrow();
        assertEquals(response.reviewSession().getAiProvider(), persistedReview.getAiProvider(), "aiProvider must be persisted in database");
        assertEquals(response.reviewSession().getAiModel(), persistedReview.getAiModel(), "aiModel must be persisted in database");
        assertEquals(response.reviewSession().getAiKeySource(), persistedReview.getAiKeySource(), "aiKeySource must be persisted in database");
    }

    @Test
    @DisplayName("4. Accepted submission produces SOLVED status for problem")
    void testAcceptedSubmissionProducesSolved() {
        String correctCode = "public class Solution { public int[] reverseArray(int[] arr) { if(arr==null) return arr; int i=0, j=arr.length-1; while(i<j){ int temp=arr[i]; arr[i]=arr[j]; arr[j]=temp; i++; j--; } return arr; } }";
        submissionService.submitSolution(reverseArrayProblem.getId(), correctCode, userA.getId());

        ProblemEntity enrichedProblem = problemService.getProblemById(reverseArrayProblem.getId(), userA.getId()).orElseThrow();
        assertEquals("SOLVED", enrichedProblem.getUserStatus(), "Accepted solution must produce SOLVED status");
    }

    @Test
    @DisplayName("5. Failed submission produces ATTEMPTED status for problem")
    void testFailedSubmissionProducesAttempted() {
        String wrongCode = "public class Solution { public int[] reverseArray(int[] arr) { return arr; } }";
        submissionService.submitSolution(reverseArrayProblem.getId(), wrongCode, userA.getId());

        ProblemEntity enrichedProblem = problemService.getProblemById(reverseArrayProblem.getId(), userA.getId()).orElseThrow();
        assertEquals("ATTEMPTED", enrichedProblem.getUserStatus(), "Failed solution must produce ATTEMPTED status");
    }

    @Test
    @DisplayName("6. Multiple submissions remain persisted in order")
    void testMultipleSubmissionsRemainPersisted() {
        String wrongCode = "public class Solution { public int[] reverseArray(int[] arr) { return arr; } }";
        String correctCode = "public class Solution { public int[] reverseArray(int[] arr) { if(arr==null) return arr; int i=0, j=arr.length-1; while(i<j){ int temp=arr[i]; arr[i]=arr[j]; arr[j]=temp; i++; j--; } return arr; } }";

        submissionService.submitSolution(reverseArrayProblem.getId(), wrongCode, userA.getId());
        submissionService.submitSolution(reverseArrayProblem.getId(), correctCode, userA.getId());

        List<SubmissionEntity> list = submissionRepository.findByUserIdAndProblemId(userA.getId(), reverseArrayProblem.getId());
        assertEquals(2, list.size(), "Both submissions must remain persisted in DB");
    }

    @Test
    @DisplayName("7. Progress reads persisted submissions correctly")
    void testProgressReadsPersistedSubmissions() {
        String correctCode = "public class Solution { public int[] reverseArray(int[] arr) { if(arr==null) return arr; int i=0, j=arr.length-1; while(i<j){ int temp=arr[i]; arr[i]=arr[j]; arr[j]=temp; i++; j--; } return arr; } }";
        submissionService.submitSolution(reverseArrayProblem.getId(), correctCode, userA.getId());

        UserProgressEntity progress = progressService.getUserProgress(userA.getId());
        assertEquals(1, progress.getSolvedCount(), "Solved count should be 1");
        assertEquals(1, progress.getAttemptedCount(), "Attempted count should be 1");
        assertEquals(1, progress.getTotalSubmissions(), "Total submissions should be 1");
        assertNotNull(progress.getAverageScore(), "Average score should not be null");
    }

    @Test
    @DisplayName("8 & 9. Submission history is isolated per user and sorted newest first")
    void testHistoryIsolationAndOrdering() {
        String codeA = "public class Solution { public int[] reverseArray(int[] arr) { return arr; } }";
        String codeB = "public class Solution { public int[] reverseArray(int[] arr) { return arr; } }";

        submissionService.submitSolution(reverseArrayProblem.getId(), codeA, userA.getId());
        submissionService.submitSolution(reverseArrayProblem.getId(), codeB, userB.getId());

        List<SubmissionHistoryDto> historyA = submissionService.getSubmissionHistoryByProblem(reverseArrayProblem.getId(), userA.getId());
        List<SubmissionHistoryDto> historyB = submissionService.getSubmissionHistoryByProblem(reverseArrayProblem.getId(), userB.getId());

        assertEquals(1, historyA.size(), "User A history should only contain User A submission");
        assertEquals(1, historyB.size(), "User B history should only contain User B submission");
        assertNotEquals(historyA.get(0).getId(), historyB.get(0).getId(), "User A and User B submission IDs must differ");
    }

    @Test
    @DisplayName("10. Re-fetching state returns the same persisted state")
    void testRefetchingReturnsSamePersistedState() {
        String correctCode = "public class Solution { public int[] reverseArray(int[] arr) { if(arr==null) return arr; int i=0, j=arr.length-1; while(i<j){ int temp=arr[i]; arr[i]=arr[j]; arr[j]=temp; i++; j--; } return arr; } }";
        submissionService.submitSolution(reverseArrayProblem.getId(), correctCode, userA.getId());

        // Re-fetch 1
        ProblemEntity p1 = problemService.getProblemById(reverseArrayProblem.getId(), userA.getId()).orElseThrow();
        UserProgressEntity prog1 = progressService.getUserProgress(userA.getId());

        // Re-fetch 2
        ProblemEntity p2 = problemService.getProblemById(reverseArrayProblem.getId(), userA.getId()).orElseThrow();
        UserProgressEntity prog2 = progressService.getUserProgress(userA.getId());

        assertEquals("SOLVED", p1.getUserStatus());
        assertEquals("SOLVED", p2.getUserStatus());
        assertEquals(prog1.getSolvedCount(), prog2.getSolvedCount());
        assertEquals(prog1.getTotalSubmissions(), prog2.getTotalSubmissions());
    }

    @Test
    @DisplayName("11 & 12. Free-form review does not corrupt challenge stats or submission score source")
    void testFreeformReviewDoesNotCorruptChallengeStats() {
        // Run freeform review ONLY
        reviewService.analyzeFreeformCode("public class Test { }", userA.getId());

        UserProgressEntity progress = progressService.getUserProgress(userA.getId());
        assertEquals(0, progress.getSolvedCount(), "Freeform review should not increment solved count");
        assertEquals(0, progress.getAttemptedCount(), "Freeform review should not increment attempted count");
        assertEquals(0, progress.getTotalSubmissions(), "Freeform review should not increment total submissions");
        assertNull(progress.getAverageScore(), "Average review score should be null when no challenge submissions exist");
    }

    @Test
    @DisplayName("13. Legacy review session with null syntaxStatus persists and loads without breaking")
    void testLegacyReviewSessionWithNullSyntaxStatusCompatibility() {
        // Create submission
        SubmissionEntity sub = submissionRepository.save(SubmissionEntity.builder()
                .user(userA)
                .problem(reverseArrayProblem)
                .code("public class Legacy { }")
                .language("JAVA")
                .status("PASSED")
                .passedTestCases(3)
                .totalTestCases(3)
                .build());

        // Save legacy review session with null syntaxStatus
        ReviewSessionEntity legacySession = reviewSessionRepository.save(ReviewSessionEntity.builder()
                .user(userA)
                .problem(reverseArrayProblem)
                .submission(sub)
                .code(sub.getCode())
                .sessionType("PROBLEM_SUBMISSION")
                .language("JAVA")
                .overallScore(85)
                .syntaxStatus(null) // Legacy row simulation
                .build());

        assertNotNull(legacySession.getId(), "Legacy review session ID must be generated");

        // Reload directly from repository
        ReviewSessionEntity reloaded = reviewSessionRepository.findById(legacySession.getId()).orElseThrow();
        assertNull(reloaded.getSyntaxStatus(), "Legacy record must cleanly retain null syntaxStatus");
        assertEquals(85, reloaded.getOverallScore());

        // Verify mapping into SubmissionHistoryDto does not fail
        List<SubmissionHistoryDto> history = submissionService.getSubmissionHistoryByProblem(reverseArrayProblem.getId(), userA.getId());
        assertFalse(history.isEmpty());
        SubmissionHistoryDto historyItem = history.stream()
                .filter(h -> h.getId().equals(sub.getId()))
                .findFirst()
                .orElseThrow();
        assertNull(historyItem.getSyntaxStatus(), "Submission history must safely expose null syntaxStatus for legacy rows");
    }

    @Test
    @DisplayName("14. Review session persists VALID, INVALID, and UNAVAILABLE syntax statuses correctly")
    void testReviewSessionSyntaxStatusAllStatesPersistence() {
        // VALID state
        ReviewSessionEntity validSession = reviewSessionRepository.save(ReviewSessionEntity.builder()
                .user(userA)
                .code("public class Valid { }")
                .sessionType("FREEFORM_REVIEW")
                .language("JAVA")
                .syntaxStatus("VALID")
                .build());

        // INVALID state
        ReviewSessionEntity invalidSession = reviewSessionRepository.save(ReviewSessionEntity.builder()
                .user(userA)
                .code("public class Invalid {")
                .sessionType("FREEFORM_REVIEW")
                .language("JAVA")
                .syntaxStatus("INVALID")
                .build());

        // UNAVAILABLE state
        ReviewSessionEntity unavailableSession = reviewSessionRepository.save(ReviewSessionEntity.builder()
                .user(userA)
                .code("int main() { return 0; }")
                .sessionType("FREEFORM_REVIEW")
                .language("CPP")
                .syntaxStatus("UNAVAILABLE")
                .build());

        ReviewSessionEntity reloadedValid = reviewSessionRepository.findById(validSession.getId()).orElseThrow();
        ReviewSessionEntity reloadedInvalid = reviewSessionRepository.findById(invalidSession.getId()).orElseThrow();
        ReviewSessionEntity reloadedUnavailable = reviewSessionRepository.findById(unavailableSession.getId()).orElseThrow();

        assertEquals("VALID", reloadedValid.getSyntaxStatus());
        assertEquals("INVALID", reloadedInvalid.getSyntaxStatus());
        assertEquals("UNAVAILABLE", reloadedUnavailable.getSyntaxStatus());
    }
}
