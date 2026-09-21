package com.staffcore33.ats.submission;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SubmissionStatusUpdateRequest {
    @NotBlank
    private String status;
    private String notes;
}
