package com.codereview.backend.service;

import com.codereview.backend.entity.ReviewSessionEntity;
import com.codereview.backend.entity.SubmissionEntity;
import com.codereview.backend.entity.UserEntity;
import com.codereview.backend.entity.UserProgressEntity;
import com.codereview.backend.repository.ReviewSessionRepository;
import com.codereview.backend.repository.SubmissionRepository;
import com.codereview.backend.repository.UserProgressRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProgressService {

    private final UserProgressRepository userProgressRepository;
    private final SubmissionRepository submissionRepository;
    private final ReviewSessionRepository reviewSessionRepository;
    private final ReviewService reviewService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Transactional
    public UserProgressEntity getUserProgress(Long userId) {
        UserEntity user = reviewService.resolveUser(userId);
        return recalculateUserProgress(user);
    }

    @Transactional
    public UserProgressEntity recalculateUserProgress(UserEntity user) {
        List<SubmissionEntity> submissions = submissionRepository.findByUserId(user.getId());
        List<ReviewSessionEntity> reviewSessions = reviewSessionRepository.findByUserId(user.getId());

        int totalSubmissions = submissions.size();

        Set<Long> attemptedProblemIds = submissions.stream()
                .filter(s -> s.getProblem() != null)
                .map(s -> s.getProblem().getId())
                .collect(Collectors.toSet());
        int attemptedCount = attemptedProblemIds.size();

        Set<Long> solvedProblemIds = submissions.stream()
                .filter(s -> s.getProblem() != null && ("PASSED".equalsIgnoreCase(s.getStatus()) || "SOLVED".equalsIgnoreCase(s.getStatus()) || "ACCEPTED".equalsIgnoreCase(s.getStatus())))
                .map(s -> s.getProblem().getId())
                .collect(Collectors.toSet());
        int solvedCount = solvedProblemIds.size();

        Double averageScore = null;
        List<Integer> validScores = reviewSessions.stream()
                .filter(s -> s.getSubmission() != null || "PROBLEM_SUBMISSION".equalsIgnoreCase(s.getSessionType()))
                .map(ReviewSessionEntity::getOverallScore)
                .filter(Objects::nonNull)
                .toList();

        if (!validScores.isEmpty()) {
            double avg = validScores.stream().mapToInt(Integer::intValue).average().orElse(0.0);
            averageScore = Math.round(avg * 10.0) / 10.0;
        }

        Map<String, Integer> topicMasteryMap = new LinkedHashMap<>();
        if (!submissions.isEmpty()) {
            Map<String, List<SubmissionEntity>> submissionsByCategory = submissions.stream()
                    .filter(s -> s.getProblem() != null && s.getProblem().getCategory() != null)
                    .collect(Collectors.groupingBy(s -> s.getProblem().getCategory()));

            for (Map.Entry<String, List<SubmissionEntity>> entry : submissionsByCategory.entrySet()) {
                String category = entry.getKey();
                List<SubmissionEntity> catSubmissions = entry.getValue();

                Set<Long> catAttempted = catSubmissions.stream()
                        .map(s -> s.getProblem().getId())
                        .collect(Collectors.toSet());

                Set<Long> catSolved = catSubmissions.stream()
                        .filter(s -> "PASSED".equalsIgnoreCase(s.getStatus()) || "SOLVED".equalsIgnoreCase(s.getStatus()) || "ACCEPTED".equalsIgnoreCase(s.getStatus()))
                        .map(s -> s.getProblem().getId())
                        .collect(Collectors.toSet());

                if (!catAttempted.isEmpty()) {
                    int pct = (int) Math.round((catSolved.size() * 100.0) / catAttempted.size());
                    topicMasteryMap.put(category, pct);
                }
            }
        }

        String topicMasteryJson = "{}";
        try {
            topicMasteryJson = objectMapper.writeValueAsString(topicMasteryMap);
        } catch (JsonProcessingException ignored) {}

        UserProgressEntity progress = userProgressRepository.findByUserId(user.getId())
                .orElseGet(() -> UserProgressEntity.builder().user(user).build());

        progress.setUser(user);
        progress.setSolvedCount(solvedCount);
        progress.setAttemptedCount(attemptedCount);
        progress.setTotalSubmissions(totalSubmissions);
        progress.setAverageScore(averageScore);
        progress.setTopicMasteryJson(topicMasteryJson);

        return userProgressRepository.save(progress);
    }
}

