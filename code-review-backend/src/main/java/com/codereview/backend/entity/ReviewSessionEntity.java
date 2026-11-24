package com.codereview.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "review_sessions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewSessionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private UserEntity user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "problem_id")
    private ProblemEntity problem;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "submission_id")
    private SubmissionEntity submission;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String code;

    @Column(name = "session_type", nullable = false)
    private String sessionType; // PROBLEM_SUBMISSION, FREEFORM_REVIEW

    @Column(name = "language")
    @Builder.Default
    private String language = "JAVA";

    @Column(name = "overall_score")
    private Integer overallScore;

    @Column(name = "correctness_score")
    private Integer correctnessScore;

    @Column(name = "quality_score")
    private Integer qualityScore;

    @Column(name = "complexity_score")
    private Integer complexityScore;

    @Column(name = "maintainability_score")
    private Integer maintainabilityScore;

    @Column(name = "security_score")
    private Integer securityScore;

    @Column(name = "performance_score")
    private Integer performanceScore;

    @Column(name = "testing_score")
    private Integer testingScore;

    @Column(name = "time_complexity")
    private String timeComplexity;

    @Column(name = "space_complexity")
    private String spaceComplexity;

    @Column(name = "syntax_status")
    private String syntaxStatus; // VALID, INVALID, UNAVAILABLE

    @Column(name = "execution_status")
    private String executionStatus;

    @Column(name = "execution_stdout", columnDefinition = "TEXT")
    private String executionStdout;

    @Column(name = "execution_stderr", columnDefinition = "TEXT")
    private String executionStderr;

    @Column(name = "execution_time_ms")
    private Long executionTimeMs;

    @Column(name = "execution_applicable")
    private Boolean executionApplicable;

    @Column(name = "execution_summary", columnDefinition = "TEXT")
    private String executionSummary;

    @Column(columnDefinition = "TEXT")
    private String summary;

    @Column(name = "ai_provider")
    private String aiProvider; // GEMINI, DETERMINISTIC

    @Column(name = "ai_model")
    private String aiModel; // gemini-3.8-flash, RULE_BASED_AST

    @Column(name = "ai_key_source")
    private String aiKeySource; // PRIMARY, BACKUP, NONE


    @Builder.Default
    @OneToMany(mappedBy = "reviewSession", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<FindingEntity> findings = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "reviewSession", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ReviewMetricEntity> metrics = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "reviewSession", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TestSuggestionEntity> testSuggestions = new ArrayList<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
