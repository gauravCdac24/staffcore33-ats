package com.staffcore33.ats.task;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.time.LocalDate;

@Data
@Builder
public class TaskResponse {
    private Long id;
    private String title;
    private String notes;
    private Long candidateId;
    private Long jobId;
    private Long clientId;
    private Long assignedTo;
    private LocalDate dueDate;
    private String priority;
    private String status;
    private Long createdBy;
    private Instant createdAt;
    private Instant updatedAt;
}
