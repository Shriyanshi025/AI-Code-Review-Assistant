package com.codereview.backend.repository;

import com.codereview.backend.entity.BadgeDefinition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BadgeDefinitionRepository extends JpaRepository<BadgeDefinition, Long> {
    Optional<BadgeDefinition> findByCode(String code);
    List<BadgeDefinition> findByActiveTrue();
}
