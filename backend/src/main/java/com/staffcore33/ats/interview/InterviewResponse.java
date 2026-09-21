package com.staffcore33.ats.interview;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
@Builder
public class InterviewResponse {
    private Long id;
    private Long candidateId;
    private Long jobId;
    private Long clientId;
    private String roundName;
    private LocalDate interviewDate;
    private LocalTime interviewTime;
    private Integer durationMinutes;
    private String interviewType;
    private String interviewer;
    private Long recruiterId;
    private String notes;
    private String feedback;
    private String result;
    private String status;
    private Instant createdAt;
    private Instant updatedAt;
}
