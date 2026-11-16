package com.codereview.backend.repository;

import com.codereview.backend.entity.TestCaseEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TestCaseRepository extends JpaRepository<TestCaseEntity, Long> {
    List<TestCaseEntity> findByProblemId(Long problemId);
    List<TestCaseEntity> findByProblemIdAndIsHiddenFalse(Long problemId);
}
