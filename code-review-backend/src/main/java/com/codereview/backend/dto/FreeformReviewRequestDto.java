package com.codereview.backend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class FreeformReviewRequestDto {
    @NotBlank(message = "Java source code cannot be empty")
    private String code;
    private Long userId;
}
