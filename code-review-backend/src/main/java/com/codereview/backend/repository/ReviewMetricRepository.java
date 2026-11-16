package com.codereview.backend.repository;

import com.codereview.backend.entity.ReviewMetricEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewMetricRepository extends JpaRepository<ReviewMetricEntity, Long> {
    List<ReviewMetricEntity> findByReviewSessionId(Long reviewSessionId);
}
