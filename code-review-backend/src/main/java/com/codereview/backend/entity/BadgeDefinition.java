package com.codereview.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "badge_definitions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BadgeDefinition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String code;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String description;

    @Column(nullable = false)
    private String icon;

    @Column(name = "requirement_type", nullable = false)
    private String requirementType; // SOLVED_COUNT, CATEGORY_SOLVED_COUNT, FIRST_ATTEMPT_SOLVES, TOPIC_COUNT, AVERAGE_REVIEW_SCORE, TOTAL_SUBMISSIONS

    @Column(name = "requirement_value", nullable = false)
    private int requirementValue;

    @Column(name = "category")
    private String category; // Optional target category, e.g., "Arrays"

    @Column(nullable = false)
    @Builder.Default
    private boolean active = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
