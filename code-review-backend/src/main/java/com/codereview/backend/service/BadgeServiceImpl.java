package com.codereview.backend.service;

import com.codereview.backend.dto.BadgeDto;
import com.codereview.backend.entity.*;
import com.codereview.backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BadgeServiceImpl implements BadgeService {

    private final BadgeDefinitionRepository badgeDefinitionRepository;
    private final UserBadgeRepository userBadgeRepository;
    private final SubmissionRepository submissionRepository;
    private final ReviewSessionRepository reviewSessionRepository;
    private final ReviewService reviewService;

    @Override
    @Transactional
    public List<BadgeDto> evaluateBadges(UserEntity user) {
        if (user == null) {
            return Collections.emptyList();
        }

        List<BadgeDefinition> activeBadges = badgeDefinitionRepository.findByActiveTrue();
        List<UserBadge> existingUserBadges = userBadgeRepository.findByUserId(user.getId());
        Set<Long> unlockedBadgeIds = existingUserBadges.stream()
                .map(ub -> ub.getBadge().getId())
                .collect(Collectors.toSet());

        List<SubmissionEntity> allSubmissions = submissionRepository.findByUserId(user.getId());
        List<ReviewSessionEntity> allReviewSessions = reviewSessionRepository.findByUserId(user.getId());

        List<BadgeDto> newlyUnlocked = new ArrayList<>();

        for (BadgeDefinition badgeDef : activeBadges) {
            boolean isAlreadyUnlocked = unlockedBadgeIds.contains(badgeDef.getId());
            EvaluationResult eval = evaluateBadgeEligibility(badgeDef, allSubmissions, allReviewSessions);

            if (!isAlreadyUnlocked && eval.eligible) {
                // Unlock badge
                if (!userBadgeRepository.existsByUserIdAndBadgeId(user.getId(), badgeDef.getId())) {
                    UserBadge ub = UserBadge.builder()
                            .user(user)
                            .badge(badgeDef)
                            .unlockedAt(LocalDateTime.now())
                            .build();
                    ub = userBadgeRepository.save(ub);

                    BadgeDto dto = mapToDto(badgeDef, true, ub.getUnlockedAt(), eval.progress, eval.target);
                    newlyUnlocked.add(dto);
                    unlockedBadgeIds.add(badgeDef.getId());
                }
            }
        }

        return newlyUnlocked;
    }

    @Override
    @Transactional
    public List<BadgeDto> getUserBadges(Long userId) {
        UserEntity user = reviewService.resolveUser(userId);
        // Automatically evaluate in case user earned badges from existing persisted activity
        evaluateBadges(user);

        List<BadgeDefinition> activeBadges = badgeDefinitionRepository.findByActiveTrue();
        List<UserBadge> userBadges = userBadgeRepository.findByUserId(user.getId());

        Map<Long, UserBadge> unlockedMap = userBadges.stream()
                .collect(Collectors.toMap(ub -> ub.getBadge().getId(), ub -> ub, (a, b) -> a));

        List<SubmissionEntity> allSubmissions = submissionRepository.findByUserId(user.getId());
        List<ReviewSessionEntity> allReviewSessions = reviewSessionRepository.findByUserId(user.getId());

        List<BadgeDto> result = new ArrayList<>();
        for (BadgeDefinition badgeDef : activeBadges) {
            UserBadge ub = unlockedMap.get(badgeDef.getId());
            boolean unlocked = ub != null;
            EvaluationResult eval = evaluateBadgeEligibility(badgeDef, allSubmissions, allReviewSessions);
            LocalDateTime unlockedAt = unlocked ? ub.getUnlockedAt() : null;

            result.add(mapToDto(badgeDef, unlocked, unlockedAt, eval.progress, eval.target));
        }

        return result;
    }

    @Override
    @Transactional
    public List<BadgeDto> getUnlockedBadges(Long userId) {
        return getUserBadges(userId).stream()
                .filter(BadgeDto::isUnlocked)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public List<BadgeDto> getBadgeProgress(Long userId) {
        return getUserBadges(userId).stream()
                .filter(b -> !b.isUnlocked())
                .collect(Collectors.toList());
    }

    private EvaluationResult evaluateBadgeEligibility(BadgeDefinition badgeDef,
                                                       List<SubmissionEntity> submissions,
                                                       List<ReviewSessionEntity> reviewSessions) {
        String reqType = badgeDef.getRequirementType();
        int reqValue = badgeDef.getRequirementValue();
        String category = badgeDef.getCategory();

        switch (reqType) {
            case "SOLVED_COUNT": {
                Set<Long> solvedProblemIds = submissions.stream()
                        .filter(s -> s.getProblem() != null && isAccepted(s.getStatus()))
                        .map(s -> s.getProblem().getId())
                        .collect(Collectors.toSet());
                int count = solvedProblemIds.size();
                return new EvaluationResult(count >= reqValue, Math.min(count, reqValue), reqValue);
            }
            case "CATEGORY_SOLVED_COUNT": {
                Set<Long> catSolvedProblemIds = submissions.stream()
                        .filter(s -> s.getProblem() != null && isAccepted(s.getStatus()))
                        .filter(s -> category != null && category.equalsIgnoreCase(s.getProblem().getCategory()))
                        .map(s -> s.getProblem().getId())
                        .collect(Collectors.toSet());
                int count = catSolvedProblemIds.size();
                return new EvaluationResult(count >= reqValue, Math.min(count, reqValue), reqValue);
            }
            case "FIRST_ATTEMPT_SOLVES": {
                // Group submissions by problem ID
                Map<Long, List<SubmissionEntity>> problemSubmissions = submissions.stream()
                        .filter(s -> s.getProblem() != null)
                        .collect(Collectors.groupingBy(s -> s.getProblem().getId()));

                int firstAttemptCount = 0;
                for (List<SubmissionEntity> subs : problemSubmissions.values()) {
                    subs.sort(Comparator.comparing(SubmissionEntity::getSubmittedAt)
                            .thenComparing(SubmissionEntity::getId));
                    if (!subs.isEmpty()) {
                        SubmissionEntity earliest = subs.get(0);
                        if (isAccepted(earliest.getStatus())) {
                            firstAttemptCount++;
                        }
                    }
                }
                return new EvaluationResult(firstAttemptCount >= reqValue, Math.min(firstAttemptCount, reqValue), reqValue);
            }
            case "TOPIC_COUNT": {
                Set<String> uniqueCategories = submissions.stream()
                        .filter(s -> s.getProblem() != null && isAccepted(s.getStatus()))
                        .map(s -> s.getProblem().getCategory())
                        .filter(Objects::nonNull)
                        .filter(c -> !c.isBlank())
                        .collect(Collectors.toSet());
                int count = uniqueCategories.size();
                return new EvaluationResult(count >= reqValue, Math.min(count, reqValue), reqValue);
            }
            case "AVERAGE_REVIEW_SCORE": {
                // Quality Coder badge: requires at least reqValue (5) accepted challenge submissions AND avg review score >= 90
                List<ReviewSessionEntity> acceptedSessions = reviewSessions.stream()
                        .filter(rs -> rs.getSubmission() != null || "PROBLEM_SUBMISSION".equalsIgnoreCase(rs.getSessionType()))
                        .filter(rs -> rs.getSubmission() != null && isAccepted(rs.getSubmission().getStatus()))
                        .filter(rs -> rs.getOverallScore() != null)
                        .toList();

                int acceptedCount = acceptedSessions.size();
                if (acceptedCount == 0) {
                    return new EvaluationResult(false, 0, reqValue);
                }

                double avgScore = acceptedSessions.stream()
                        .mapToInt(ReviewSessionEntity::getOverallScore)
                        .average()
                        .orElse(0.0);

                boolean eligible = (acceptedCount >= reqValue) && (avgScore >= 90.0);
                
                // For progress rendering:
                // If user doesn't have min required submissions, show acceptedCount / reqValue.
                // If user has required submissions, show avgScore / 90.
                if (acceptedCount < reqValue) {
                    return new EvaluationResult(eligible, acceptedCount, reqValue);
                } else {
                    int roundedAvg = (int) Math.round(avgScore);
                    return new EvaluationResult(eligible, roundedAvg, 90);
                }
            }
            case "TOTAL_SUBMISSIONS": {
                int count = submissions.size();
                return new EvaluationResult(count >= reqValue, Math.min(count, reqValue), reqValue);
            }
            default:
                return new EvaluationResult(false, 0, reqValue);
        }
    }

    private boolean isAccepted(String status) {
        if (status == null) return false;
        return "PASSED".equalsIgnoreCase(status) ||
               "ACCEPTED".equalsIgnoreCase(status) ||
               "SOLVED".equalsIgnoreCase(status);
    }

    private BadgeDto mapToDto(BadgeDefinition badgeDef, boolean unlocked, LocalDateTime unlockedAt, int progress, int target) {
        return BadgeDto.builder()
                .code(badgeDef.getCode())
                .name(badgeDef.getName())
                .description(badgeDef.getDescription())
                .icon(badgeDef.getIcon())
                .requirementType(badgeDef.getRequirementType())
                .requirementValue(badgeDef.getRequirementValue())
                .category(badgeDef.getCategory())
                .unlocked(unlocked)
                .unlockedAt(unlockedAt)
                .progress(unlocked ? target : progress)
                .target(target)
                .build();
    }

    private record EvaluationResult(boolean eligible, int progress, int target) {}
}
