package com.staffcore33.ats.activity;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.Instant;

@Data
public class ActivityRequest {

    @NotBlank
    private String activityType;

    private String notes;
    private Instant activityAt;
    private Long candidateId;
    private Long jobId;
    private Long clientId;
}
