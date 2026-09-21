package com.staffcore33.ats.activity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "activities")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Activity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "activity_type", nullable = false)
    private String activityType;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "activity_at", nullable = false)
    private Instant activityAt;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "candidate_id")
    private Long candidateId;

    @Column(name = "job_id")
    private Long jobId;

    @Column(name = "client_id")
    private Long clientId;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    @PrePersist
    void onCreate() {
        Instant now = Instant.now();
        if (createdAt == null) {
            createdAt = now;
        }
        if (activityAt == null) {
            activityAt = now;
        }
    }
}
