package com.codereview.backend.service;

import com.codereview.backend.dto.BadgeDto;
import com.codereview.backend.dto.SubmissionHistoryDto;
import com.codereview.backend.entity.*;
import com.codereview.backend.execution.ExecutionEngine;
import com.codereview.backend.execution.ExecutionRequest;
import com.codereview.backend.execution.ExecutionResult;
import com.codereview.backend.repository.ProblemRepository;
import com.codereview.backend.repository.ReviewSessionRepository;
import com.codereview.backend.repository.SubmissionRepository;
import com.codereview.backend.review.ReviewEngine;
import com.codereview.backend.util.LanguageValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SubmissionService {

    private final SubmissionRepository submissionRepository;
    private final ProblemRepository problemRepository;
    private final ExecutionEngine executionEngine;
    private final ReviewEngine reviewEngine;
    private final ReviewSessionRepository reviewSessionRepository;
    private final ProgressService progressService;
    private final ReviewService reviewService;
    private final BadgeService badgeService;

    @Transactional
    public SubmissionResponse submitSolution(Long problemId, String code, Long userId) {
        return submitSolution(problemId, code, userId, "JAVA");
    }

    @Transactional
    public SubmissionResponse submitSolution(Long problemId, String code, Long userId, String language) {
        UserEntity user = reviewService.resolveUser(userId);
        ProblemEntity problem = problemRepository.findById(problemId)
                .orElseThrow(() -> new IllegalArgumentException("Problem not found with ID: " + problemId));

        String lang = LanguageValidator.validateAndNormalize(language);

        // 1. Run Execution Engine
        ExecutionResult execResult = executionEngine.execute(ExecutionRequest.builder()
                .code(code)
                .language(lang)
                .testCases(problem.getTestCases())
                .build());

        // 2. Save Submission Entity
        SubmissionEntity submission = SubmissionEntity.builder()
                .user(user)
                .problem(problem)
                .code(code)
                .language(lang)
                .status(execResult.getStatus())
                .passedTestCases(execResult.getPassedTestCases())
                .totalTestCases(execResult.getTotalTestCases())
                .executionTimeMs(execResult.getExecutionTimeMs())
                .memoryUsedKb(execResult.getMemoryUsedKb())
                .build();
        submission = submissionRepository.save(submission);

        // 3. Perform Unified Code Review
        ReviewSessionEntity reviewSession = reviewEngine.createReviewSession(user, problem, submission, code, "PROBLEM_SUBMISSION", lang);
        reviewSession = reviewSessionRepository.save(reviewSession);

        // 4. Update User Progress (dynamically recalculated from database)
        progressService.recalculateUserProgress(user);

        // 5. Evaluate Badges (persisted in database)
        List<BadgeDto> newlyUnlockedBadges = badgeService.evaluateBadges(user);

        return new SubmissionResponse(submission, reviewSession, execResult, newlyUnlockedBadges);
    }

    public List<SubmissionEntity> getSubmissionsByProblem(Long problemId) {
        return submissionRepository.findByProblemId(problemId);
    }

    public List<SubmissionHistoryDto> getSubmissionHistoryByProblem(Long problemId, Long userId) {
        UserEntity user = reviewService.resolveUser(userId);
        List<SubmissionEntity> submissions = submissionRepository.findByUserIdAndProblemIdOrderBySubmittedAtDesc(user.getId(), problemId);

        List<SubmissionHistoryDto> historyList = new ArrayList<>();
        for (SubmissionEntity sub : submissions) {
            List<ReviewSessionEntity> sessions = reviewSessionRepository.findBySubmissionId(sub.getId());
            ReviewSessionEntity session = sessions.isEmpty() ? null : sessions.get(0);

            SubmissionHistoryDto dto = SubmissionHistoryDto.builder()
                    .id(sub.getId())
                    .problemId(sub.getProblem() != null ? sub.getProblem().getId() : problemId)
                    .problemTitle(sub.getProblem() != null ? sub.getProblem().getTitle() : null)
                    .code(sub.getCode())
                    .language(sub.getLanguage())
                    .status(sub.getStatus())
                    .passedTestCases(sub.getPassedTestCases())
                    .totalTestCases(sub.getTotalTestCases())
                    .executionTimeMs(sub.getExecutionTimeMs())
                    .memoryUsedKb(sub.getMemoryUsedKb())
                    .submittedAt(sub.getSubmittedAt())
                    .overallScore(session != null ? session.getOverallScore() : null)
                    .correctnessScore(session != null ? session.getCorrectnessScore() : null)
                    .qualityScore(session != null ? session.getQualityScore() : null)
                    .complexityScore(session != null ? session.getComplexityScore() : null)
                    .timeComplexity(session != null ? session.getTimeComplexity() : null)
                    .spaceComplexity(session != null ? session.getSpaceComplexity() : null)
                    .syntaxStatus(session != null ? session.getSyntaxStatus() : null)
                    .summary(session != null ? session.getSummary() : null)
                    .build();

            historyList.add(dto);
        }

        return historyList;
    }

    public record SubmissionResponse(
            SubmissionEntity submission,
            ReviewSessionEntity reviewSession,
            ExecutionResult executionResult,
            List<BadgeDto> newlyUnlockedBadges
    ) {
        public SubmissionResponse(SubmissionEntity submission, ReviewSessionEntity reviewSession, ExecutionResult executionResult) {
            this(submission, reviewSession, executionResult, List.of());
        }
    }
}
