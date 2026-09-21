package com.staffcore33.ats.interview;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class InterviewRequest {

    @NotNull
    private Long candidateId;

    @NotNull
    private Long jobId;

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
}
