package com.codereview.backend.repository;

import com.codereview.backend.entity.HintEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HintRepository extends JpaRepository<HintEntity, Long> {
    List<HintEntity> findByProblemIdOrderByHintOrderAsc(Long problemId);
}
