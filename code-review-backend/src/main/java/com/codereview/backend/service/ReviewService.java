package com.codereview.backend.service;

import com.codereview.backend.entity.ProblemEntity;
import com.codereview.backend.entity.ReviewSessionEntity;
import com.codereview.backend.entity.UserEntity;
import com.codereview.backend.repository.ProblemRepository;
import com.codereview.backend.repository.ReviewSessionRepository;
import com.codereview.backend.repository.UserRepository;
import com.codereview.backend.review.ReviewEngine;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewEngine reviewEngine;
    private final ReviewSessionRepository reviewSessionRepository;
    private final ProblemRepository problemRepository;
    private final UserRepository userRepository;

    @Transactional
    public ReviewSessionEntity analyzeFreeformCode(String code, Long userId) {
        UserEntity user = resolveUser(userId);

        ReviewSessionEntity session = reviewEngine.createReviewSession(
                user, null, null, code, "FREEFORM_REVIEW");

        return reviewSessionRepository.save(session);
    }

    @Transactional
    public ReviewSessionEntity analyzeProblemCode(Long problemId, String code, Long userId) {
        UserEntity user = resolveUser(userId);
        ProblemEntity problem = problemRepository.findById(problemId)
                .orElseThrow(() -> new IllegalArgumentException("Problem not found with ID: " + problemId));

        ReviewSessionEntity session = reviewEngine.createReviewSession(
                user, problem, null, code, "PROBLEM_SUBMISSION");

        return reviewSessionRepository.save(session);
    }

    public Optional<ReviewSessionEntity> getReviewSessionById(Long id) {
        return reviewSessionRepository.findById(id);
    }

    public List<ReviewSessionEntity> getUserReviewHistory(Long userId) {
        return reviewSessionRepository.findByUserId(userId);
    }

    public UserEntity resolveUser(Long userId) {
        if (userId != null) {
            return userRepository.findById(userId).orElseGet(this::getOrCreateDefaultUser);
        }
        return getOrCreateDefaultUser();
    }

    public UserEntity getOrCreateDefaultUser() {
        return userRepository.findByUsername("demo_developer").orElseGet(() -> userRepository.save(UserEntity.builder()
                .username("demo_developer")
                .email("developer@codeeval.ai")
                .role("STUDENT")
                .build()));
    }
}
