package com.codereview.backend.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "review_metrics")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewMetricEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "review_session_id", nullable = false)
    @JsonIgnore
    private ReviewSessionEntity reviewSession;

    @Column(name = "metric_key", nullable = false)
    private String metricKey;

    @Column(name = "metric_name", nullable = false)
    private String metricName;

    @Column(name = "metric_value", nullable = false)
    private String metricValue;

    @Column(name = "metric_category")
    private String metricCategory;

    @Column(columnDefinition = "TEXT")
    private String description;
}
