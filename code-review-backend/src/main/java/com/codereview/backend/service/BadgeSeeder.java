package com.codereview.backend.service;

import com.codereview.backend.entity.BadgeDefinition;
import com.codereview.backend.repository.BadgeDefinitionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class BadgeSeeder implements CommandLineRunner {

    private final BadgeDefinitionRepository badgeDefinitionRepository;

    @Override
    public void run(String... args) throws Exception {
        seedInitialBadges();
    }

    public void seedInitialBadges() {
        List<BadgeDefinition> initialBadges = List.of(
                BadgeDefinition.builder()
                        .code("FIRST_STEP")
                        .name("First Step")
                        .description("Solve 1 challenge successfully.")
                        .icon("🎯")
                        .requirementType("SOLVED_COUNT")
                        .requirementValue(1)
                        .active(true)
                        .build(),
                BadgeDefinition.builder()
                        .code("PROBLEM_SOLVER")
                        .name("Problem Solver")
                        .description("Solve 5 challenges successfully.")
                        .icon("🔥")
                        .requirementType("SOLVED_COUNT")
                        .requirementValue(5)
                        .active(true)
                        .build(),
                BadgeDefinition.builder()
                        .code("RISING_CODER")
                        .name("Rising Coder")
                        .description("Solve 10 challenges successfully.")
                        .icon("🚀")
                        .requirementType("SOLVED_COUNT")
                        .requirementValue(10)
                        .active(true)
                        .build(),
                BadgeDefinition.builder()
                        .code("ARRAY_EXPLORER")
                        .name("Array Explorer")
                        .description("Successfully solve 5 Array problems.")
                        .icon("🧩")
                        .requirementType("CATEGORY_SOLVED_COUNT")
                        .requirementValue(5)
                        .category("Arrays")
                        .active(true)
                        .build(),
                BadgeDefinition.builder()
                        .code("TOPIC_EXPLORER")
                        .name("Topic Explorer")
                        .description("Successfully solve problems from at least 5 different categories.")
                        .icon("🗺️")
                        .requirementType("TOPIC_COUNT")
                        .requirementValue(5)
                        .active(true)
                        .build(),
                BadgeDefinition.builder()
                        .code("FIRST_ATTEMPT_ACE")
                        .name("First-Attempt Ace")
                        .description("Successfully solve 5 problems on the first submission attempt.")
                        .icon("⚡")
                        .requirementType("FIRST_ATTEMPT_SOLVES")
                        .requirementValue(5)
                        .active(true)
                        .build(),
                BadgeDefinition.builder()
                        .code("QUALITY_CODER")
                        .name("Quality Coder")
                        .description("Maintain an average review score of at least 90 across at least 5 accepted challenge submissions.")
                        .icon("💎")
                        .requirementType("AVERAGE_REVIEW_SCORE")
                        .requirementValue(5)
                        .active(true)
                        .build(),
                BadgeDefinition.builder()
                        .code("DEDICATED_SOLVER")
                        .name("Dedicated Solver")
                        .description("Solve 25 challenges successfully.")
                        .icon("🌟")
                        .requirementType("SOLVED_COUNT")
                        .requirementValue(25)
                        .active(true)
                        .build(),
                BadgeDefinition.builder()
                        .code("CHALLENGE_MASTER")
                        .name("Challenge Master")
                        .description("Solve 50 challenges successfully.")
                        .icon("👑")
                        .requirementType("SOLVED_COUNT")
                        .requirementValue(50)
                        .active(true)
                        .build()
        );

        for (BadgeDefinition badge : initialBadges) {
            if (badgeDefinitionRepository.findByCode(badge.getCode()).isEmpty()) {
                badgeDefinitionRepository.save(badge);
            }
        }
    }
}
