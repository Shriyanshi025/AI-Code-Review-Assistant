package com.codereview.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "problems")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProblemEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String title;

    @Column(nullable = false, unique = true)
    private String slug;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String description;

    @Column(nullable = false)
    private String difficulty; // EASY, MEDIUM, HARD

    @Column(nullable = false)
    private String category; // Arrays, Strings, Searching, Sorting, Dynamic Programming, etc.

    @Column(columnDefinition = "TEXT")
    private String constraints;

    @Column(columnDefinition = "TEXT")
    private String inputFormat;

    @Column(columnDefinition = "TEXT")
    private String outputFormat;

    @Column(columnDefinition = "TEXT")
    private String examplesJson; // Structured JSON array of examples

    @Column(columnDefinition = "TEXT", nullable = false)
    private String initialCode;

    @Builder.Default
    @OneToMany(mappedBy = "problem", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TestCaseEntity> testCases = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "problem", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<HintEntity> hints = new ArrayList<>();

    @Column(name = "expected_time_complexity")
    private String expectedTimeComplexity; // e.g. "O(N)", "O(1)", "O(N log N)"

    @Column(name = "expected_space_complexity")
    private String expectedSpaceComplexity; // e.g. "O(1)", "O(N)"

    @Column(name = "in_place_required")
    private Boolean inPlaceRequired; // true if in-place array/data structure modification is expected

    @Transient
    private String userStatus; // SOLVED, ATTEMPTED, NOT_ATTEMPTED

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
