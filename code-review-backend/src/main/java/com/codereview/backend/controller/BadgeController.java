package com.codereview.backend.controller;

import com.codereview.backend.dto.BadgeDto;
import com.codereview.backend.entity.UserEntity;
import com.codereview.backend.service.BadgeService;
import com.codereview.backend.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/badges")
@RequiredArgsConstructor
public class BadgeController {

    private final BadgeService badgeService;
    private final ReviewService reviewService;

    @GetMapping
    public ResponseEntity<List<BadgeDto>> getAllBadges(@RequestParam(required = false) Long userId) {
        return ResponseEntity.ok(badgeService.getUserBadges(userId));
    }

    @GetMapping("/unlocked")
    public ResponseEntity<List<BadgeDto>> getUnlockedBadges(@RequestParam(required = false) Long userId) {
        return ResponseEntity.ok(badgeService.getUnlockedBadges(userId));
    }

    @GetMapping("/progress")
    public ResponseEntity<List<BadgeDto>> getBadgeProgress(@RequestParam(required = false) Long userId) {
        return ResponseEntity.ok(badgeService.getBadgeProgress(userId));
    }

    @PostMapping("/evaluate")
    public ResponseEntity<List<BadgeDto>> evaluateBadges(@RequestParam(required = false) Long userId) {
        UserEntity user = reviewService.resolveUser(userId);
        return ResponseEntity.ok(badgeService.evaluateBadges(user));
    }
}
