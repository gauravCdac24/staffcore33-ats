package com.staffcore33.ats.pipeline;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class JobCandidateRequest {

    @NotNull
    private Long jobId;

    @NotNull
    private Long candidateId;

    private String status;
    private String notes;
}
