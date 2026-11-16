package com.codereview.backend.repository;

import com.codereview.backend.entity.TestSuggestionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TestSuggestionRepository extends JpaRepository<TestSuggestionEntity, Long> {
    List<TestSuggestionEntity> findByReviewSessionId(Long reviewSessionId);
}
