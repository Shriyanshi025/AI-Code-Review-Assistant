package com.codereview.backend.controller;

import com.codereview.backend.entity.ProblemEntity;
import com.codereview.backend.service.ProblemService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/problems")
@RequiredArgsConstructor
public class ProblemController {

    private final ProblemService problemService;

    @GetMapping
    public ResponseEntity<List<ProblemEntity>> getAllProblems(
            @RequestParam(required = false) String difficulty,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Long userId) {

        if (difficulty != null && !difficulty.trim().isEmpty()) {
            return ResponseEntity.ok(problemService.getProblemsByDifficulty(difficulty.toUpperCase(), userId));
        }
        if (category != null && !category.trim().isEmpty()) {
            return ResponseEntity.ok(problemService.getProblemsByCategory(category, userId));
        }
        return ResponseEntity.ok(problemService.getAllProblems(userId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProblemEntity> getProblemById(
            @PathVariable Long id,
            @RequestParam(required = false) Long userId) {
        return problemService.getProblemById(id, userId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/slug/{slug}")
    public ResponseEntity<ProblemEntity> getProblemBySlug(
            @PathVariable String slug,
            @RequestParam(required = false) Long userId) {
        return problemService.getProblemBySlug(slug, userId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
