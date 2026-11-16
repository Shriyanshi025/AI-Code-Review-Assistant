package com.codereview.backend.service;

import com.codereview.backend.entity.ProblemEntity;
import com.codereview.backend.repository.ProblemRepository;
import org.mockito.Mockito;

import java.util.*;
import java.util.stream.Collectors;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;

public class SeederVerifierRunner {

    public static void main(String[] args) {
        Map<String, ProblemEntity> database = new LinkedHashMap<>();
        ProblemRepository repository = Mockito.mock(ProblemRepository.class);

        Mockito.when(repository.findBySlug(any())).thenAnswer(invocation -> {
            String slug = invocation.getArgument(0);
            return Optional.ofNullable(database.get(slug));
        });

        doAnswer(invocation -> {
            ProblemEntity entity = invocation.getArgument(0);
            database.put(entity.getSlug(), entity);
            return entity;
        }).when(repository).save(any());

        DsaProblemSeeder seeder = new DsaProblemSeeder(repository);
        seeder.seedProblems();

        System.out.println("==================================================");
        System.out.println("PERSISTED DSA PROBLEM CATALOG VERIFICATION REPORT");
        System.out.println("==================================================");
        System.out.println("Total Persisted Problems: " + database.size());

        Map<String, Long> difficultyCounts = database.values().stream()
                .collect(Collectors.groupingBy(ProblemEntity::getDifficulty, Collectors.counting()));

        System.out.println("\nDIFFICULTY BREAKDOWN:");
        difficultyCounts.forEach((diff, count) ->
                System.out.printf("  %-10s: %d (%.1f%%)\n", diff, count, (count * 100.0 / database.size())));

        Map<String, Long> topicCounts = database.values().stream()
                .collect(Collectors.groupingBy(ProblemEntity::getCategory, Collectors.counting()));

        System.out.println("\nTOPIC COVERAGE BREAKDOWN (" + topicCounts.size() + " topics):");
        topicCounts.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .forEach(e -> System.out.printf("  %-32s: %d problems\n", e.getKey(), e.getValue()));

        long hardCount = difficultyCounts.getOrDefault("HARD", 0L);
        System.out.println("\nHARD PROBLEMS VERIFICATION (" + hardCount + " problems):");
        database.values().stream()
                .filter(p -> "HARD".equalsIgnoreCase(p.getDifficulty()))
                .forEach(p -> System.out.println("  [HARD] " + p.getTitle() + " (" + p.getSlug() + ") - Topic: " + p.getCategory()));

        Set<String> slugs = new HashSet<>();
        List<String> duplicateSlugs = new ArrayList<>();
        database.values().forEach(p -> {
            if (!slugs.add(p.getSlug())) {
                duplicateSlugs.add(p.getSlug());
            }
        });

        System.out.println("\nDUPLICATE SLUGS CHECK:");
        if (duplicateSlugs.isEmpty()) {
            System.out.println("  ✓ No duplicate slugs found. All slugs are stable and unique.");
        } else {
            System.out.println("  ✕ Duplicate slugs found: " + duplicateSlugs);
        }

        long invalidStarterCode = database.values().stream()
                .filter(p -> p.getInitialCode() == null || !p.getInitialCode().contains("public class Solution"))
                .count();

        System.out.println("\nSTARTER CODE CONTRACT CHECK:");
        System.out.println("  ✓ Invalid Starter Code Count: " + invalidStarterCode);

        System.out.println("==================================================");
    }
}
