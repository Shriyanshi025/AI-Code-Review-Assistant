package com.codereview.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BadgeDto {
    private String code;
    private String name;
    private String description;
    private String icon;
    private String requirementType;
    private int requirementValue;
    private String category;
    private boolean unlocked;
    private LocalDateTime unlockedAt;
    private int progress;
    private int target;
}
