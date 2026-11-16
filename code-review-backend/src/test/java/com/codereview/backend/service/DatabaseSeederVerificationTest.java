package com.codereview.backend.service;

import com.codereview.backend.analysis.java.JavaLanguageAnalyzer;
import com.codereview.backend.entity.ProblemEntity;
import com.codereview.backend.repository.ProblemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;

class DatabaseSeederVerificationTest {

    private ProblemRepository problemRepository;
    private DsaProblemSeeder seeder;
    private final Map<String, ProblemEntity> database = new HashMap<>();

    @BeforeEach
    void setUp() {
        database.clear();
        problemRepository = Mockito.mock(ProblemRepository.class);

        Mockito.when(problemRepository.findBySlug(any())).thenAnswer(invocation -> {
            String slug = invocation.getArgument(0);
            return Optional.ofNullable(database.get(slug));
        });

        doAnswer(invocation -> {
            ProblemEntity entity = invocation.getArgument(0);
            database.put(entity.getSlug(), entity);
            return entity;
        }).when(problemRepository).save(any());

        seeder = new DsaProblemSeeder(problemRepository);
    }

    @Test
    @DisplayName("Verify problem catalog expansion reaches 100-120 problems with valid distributions")
    void testProblemCatalogExpansion() {
        seeder.seedProblems();

        int totalCount = database.size();
        System.out.println("Total Persisted Problems: " + totalCount);
        assertTrue(totalCount >= 100 && totalCount <= 150, "Total problems should be at least 100, got: " + totalCount);

        long easyCount = database.values().stream().filter(p -> "EASY".equalsIgnoreCase(p.getDifficulty())).count();
        long mediumCount = database.values().stream().filter(p -> "MEDIUM".equalsIgnoreCase(p.getDifficulty())).count();
        long hardCount = database.values().stream().filter(p -> "HARD".equalsIgnoreCase(p.getDifficulty())).count();

        System.out.println("Easy Count: " + easyCount);
        System.out.println("Medium Count: " + mediumCount);
        System.out.println("Hard Count: " + hardCount);

        assertTrue(easyCount >= 40, "Easy problems count should be >= 40, got: " + easyCount);
        assertTrue(mediumCount >= 40, "Medium problems count should be >= 40, got: " + mediumCount);
        assertTrue(hardCount >= 15, "Hard problems count should be >= 15, got: " + hardCount);

        long uniqueSlugs = database.values().stream().map(ProblemEntity::getSlug).distinct().count();
        assertEquals(totalCount, uniqueSlugs, "All problem slugs must be unique!");

        Set<String> categories = new HashSet<>();
        database.values().forEach(p -> {
            categories.add(p.getCategory());
            assertNotNull(p.getTitle(), "Problem title cannot be null");
            assertNotNull(p.getInitialCode(), "Starter code cannot be null");
            assertTrue(p.getInitialCode().contains("public class Solution"), "Starter code must use Solution class");
            assertFalse(p.getTestCases().isEmpty(), "Every problem must have test cases");
            assertFalse(p.getHints().isEmpty(), "Every problem must have hints");
        });

        System.out.println("Unique Topic Categories Covered: " + categories.size());
        assertTrue(categories.size() >= 25, "Topic coverage should span at least 25 categories");
    }
}
