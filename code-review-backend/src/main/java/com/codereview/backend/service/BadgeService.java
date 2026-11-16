package com.codereview.backend.service;

import com.codereview.backend.dto.BadgeDto;
import com.codereview.backend.entity.UserEntity;

import java.util.List;

public interface BadgeService {
    List<BadgeDto> evaluateBadges(UserEntity user);
    List<BadgeDto> getUserBadges(Long userId);
    List<BadgeDto> getUnlockedBadges(Long userId);
    List<BadgeDto> getBadgeProgress(Long userId);
}
