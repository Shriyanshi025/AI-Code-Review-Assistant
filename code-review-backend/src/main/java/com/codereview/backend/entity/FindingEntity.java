package com.codereview.backend.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "findings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FindingEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "review_session_id", nullable = false)
    @JsonIgnore
    private ReviewSessionEntity reviewSession;

    @Column(name = "rule_id", nullable = false)
    private String ruleId;

    @Column(nullable = false)
    private String severity; // HIGH, MEDIUM, LOW, INFO

    @Column(nullable = false)
    private String category; // CORRECTNESS, CODE_QUALITY, COMPLEXITY, MAINTAINABILITY, SECURITY, PERFORMANCE, TESTING

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String message;

    @Column(columnDefinition = "TEXT")
    private String explanation;

    @Column(columnDefinition = "TEXT")
    private String recommendation;

    @Column(columnDefinition = "TEXT")
    private String hint;

    @Column(name = "line_start")
    private Integer lineStart;

    @Column(name = "line_end")
    private Integer lineEnd;

    @Column(name = "column_start")
    private Integer columnStart;

    @Column(name = "column_end")
    private Integer columnEnd;

    @Column(columnDefinition = "TEXT", name = "code_snippet")
    private String codeSnippet;

    private Double confidence;

    @Column(nullable = false)
    private String source; // DETERMINISTIC, AI
}
