package com.staffcore33.ats.submission;

import com.staffcore33.ats.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "submissions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Submission extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "candidate_id", nullable = false)
    private Long candidateId;

    @Column(name = "job_id", nullable = false)
    private Long jobId;

    @Column(name = "client_id", nullable = false)
    private Long clientId;

    @Column(name = "resume_id")
    private Long resumeId;

    @Column(name = "submitted_by")
    private Long submittedBy;

    @Column(name = "submission_date", nullable = false)
    private LocalDate submissionDate;

    @Column(name = "submitted_rate", precision = 12, scale = 2)
    private BigDecimal submittedRate;

    @Column(name = "bill_rate", precision = 12, scale = 2)
    private BigDecimal billRate;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(nullable = false)
    private String status = "SUBMITTED";
}
