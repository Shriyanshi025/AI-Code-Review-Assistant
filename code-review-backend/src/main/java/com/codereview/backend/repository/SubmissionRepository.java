package com.codereview.backend.repository;

import com.codereview.backend.entity.SubmissionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SubmissionRepository extends JpaRepository<SubmissionEntity, Long> {
    List<SubmissionEntity> findByUserId(Long userId);
    List<SubmissionEntity> findByProblemId(Long problemId);
    List<SubmissionEntity> findByUserIdAndProblemId(Long userId, Long problemId);
    List<SubmissionEntity> findByUserIdAndProblemIdOrderBySubmittedAtDesc(Long userId, Long problemId);
}
