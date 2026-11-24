package com.codereview.backend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class FreeformReviewRequestDto {
    @NotBlank(message = "Source code cannot be empty")
    private String code;
    private Long userId;
    private String language;
}
