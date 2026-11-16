package com.codereview.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_progress")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserProgressEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private UserEntity user;

    @Column(name = "solved_count", nullable = false)
    private int solvedCount;

    @Column(name = "attempted_count", nullable = false)
    private int attemptedCount;

    @Column(name = "total_submissions", nullable = false)
    private int totalSubmissions;

    @Column(name = "average_score")
    private Double averageScore;

    @Column(columnDefinition = "TEXT", name = "topic_mastery_json")
    private String topicMasteryJson;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
