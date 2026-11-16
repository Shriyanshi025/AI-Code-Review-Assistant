package com.codereview.backend.repository;

import com.codereview.backend.entity.ProblemEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProblemRepository extends JpaRepository<ProblemEntity, Long> {
    Optional<ProblemEntity> findBySlug(String slug);
    List<ProblemEntity> findByDifficulty(String difficulty);
    List<ProblemEntity> findByCategory(String category);
}
