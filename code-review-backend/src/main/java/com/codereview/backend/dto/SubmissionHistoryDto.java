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
public class SubmissionHistoryDto {
    private Long id;
    private Long problemId;
    private String problemTitle;
    private String code;
    private String language;
    private String status;
    private int passedTestCases;
    private int totalTestCases;
    private Long executionTimeMs;
    private Long memoryUsedKb;
    private LocalDateTime submittedAt;
    private Integer overallScore;
    private Integer correctnessScore;
    private Integer qualityScore;
    private Integer complexityScore;
    private String timeComplexity;
    private String spaceComplexity;
    private String summary;
}
