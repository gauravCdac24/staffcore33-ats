package com.staffcore33.ats.submission;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class SubmissionRequest {

    @NotNull
    private Long candidateId;

    @NotNull
    private Long jobId;

    private Long resumeId;
    private LocalDate submissionDate;
    private BigDecimal submittedRate;
    private BigDecimal billRate;
    private String notes;
    private String status;
    private Boolean force;
}
