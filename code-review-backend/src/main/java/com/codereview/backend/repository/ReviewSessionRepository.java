package com.codereview.backend.repository;

import com.codereview.backend.entity.ReviewSessionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewSessionRepository extends JpaRepository<ReviewSessionEntity, Long> {
    List<ReviewSessionEntity> findByUserId(Long userId);
    List<ReviewSessionEntity> findByProblemId(Long problemId);
    List<ReviewSessionEntity> findBySubmissionId(Long submissionId);
    List<ReviewSessionEntity> findBySessionType(String sessionType);
}
