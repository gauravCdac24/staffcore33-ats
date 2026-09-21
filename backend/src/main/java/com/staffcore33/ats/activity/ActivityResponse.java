package com.staffcore33.ats.activity;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class ActivityResponse {
    private Long id;
    private String activityType;
    private String notes;
    private Instant activityAt;
    private Long userId;
    private Long candidateId;
    private Long jobId;
    private Long clientId;
    private Instant createdAt;
}
