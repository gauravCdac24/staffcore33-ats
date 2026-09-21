package com.staffcore33.ats.submission;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Data
@Builder
public class SubmissionResponse {
    private Long id;
    private Long candidateId;
    private Long jobId;
    private Long clientId;
    private Long resumeId;
    private Long submittedBy;
    private LocalDate submissionDate;
    private BigDecimal submittedRate;
    private BigDecimal billRate;
    private String notes;
    private String status;
    private Instant createdAt;
    private Instant updatedAt;
}
