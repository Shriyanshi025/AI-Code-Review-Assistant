package com.codereview.backend.repository;

import com.codereview.backend.entity.FindingEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FindingRepository extends JpaRepository<FindingEntity, Long> {
    List<FindingEntity> findByReviewSessionId(Long reviewSessionId);
    List<FindingEntity> findByReviewSessionIdAndSeverity(Long reviewSessionId, String severity);
}
