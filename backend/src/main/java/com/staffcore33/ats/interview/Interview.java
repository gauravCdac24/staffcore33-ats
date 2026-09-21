package com.staffcore33.ats.interview;

import com.staffcore33.ats.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "interviews")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Interview extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "candidate_id", nullable = false)
    private Long candidateId;

    @Column(name = "job_id", nullable = false)
    private Long jobId;

    @Column(name = "client_id", nullable = false)
    private Long clientId;

    @Column(name = "round_name")
    private String roundName;

    @Column(name = "interview_date")
    private LocalDate interviewDate;

    @Column(name = "interview_time")
    private LocalTime interviewTime;

    @Column(name = "duration_minutes")
    private Integer durationMinutes;

    @Column(name = "interview_type")
    private String interviewType;

    private String interviewer;

    @Column(name = "recruiter_id")
    private Long recruiterId;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(columnDefinition = "TEXT")
    private String feedback;

    private String result;

    @Column(nullable = false)
    private String status = "SCHEDULED";
}
