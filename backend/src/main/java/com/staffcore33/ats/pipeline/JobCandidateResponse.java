package com.staffcore33.ats.pipeline;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class JobCandidateResponse {
    private Long id;
    private Long jobId;
    private Long candidateId;
    private String status;
    private String notes;
    private Long createdBy;
    private Instant createdAt;
    private Instant updatedAt;
}
