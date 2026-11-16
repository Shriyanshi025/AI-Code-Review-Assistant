package com.codereview.backend.controller;

import com.codereview.backend.dto.FreeformReviewRequestDto;
import com.codereview.backend.dto.ProblemReviewRequestDto;
import com.codereview.backend.entity.ReviewSessionEntity;
import com.codereview.backend.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping("/freeform")
    public ResponseEntity<ReviewSessionEntity> analyzeFreeformCode(@Valid @RequestBody FreeformReviewRequestDto request) {
        ReviewSessionEntity session = reviewService.analyzeFreeformCode(request.getCode(), request.getUserId());
        return ResponseEntity.ok(session);
    }

    @PostMapping("/analyze")
    public ResponseEntity<ReviewSessionEntity> analyzeProblemCode(@Valid @RequestBody ProblemReviewRequestDto request) {
        ReviewSessionEntity session = reviewService.analyzeProblemCode(request.getProblemId(), request.getCode(), request.getUserId());
        return ResponseEntity.ok(session);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReviewSessionEntity> getReviewSessionById(@PathVariable Long id) {
        return reviewService.getReviewSessionById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<ReviewSessionEntity>> getUserReviewHistory(@PathVariable Long userId) {
        return ResponseEntity.ok(reviewService.getUserReviewHistory(userId));
    }
}
