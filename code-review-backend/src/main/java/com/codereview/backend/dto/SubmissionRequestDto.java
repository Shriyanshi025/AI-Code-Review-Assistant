package com.codereview.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SubmissionRequestDto {
    @NotNull(message = "Problem ID is required")
    private Long problemId;

    @NotBlank(message = "Source code cannot be empty")
    private String code;

    private Long userId;

    private String language;
}
