package com.staffcore33.ats.task;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDate;

@Data
public class TaskRequest {

    @NotBlank
    private String title;

    private String notes;
    private Long candidateId;
    private Long jobId;
    private Long clientId;
    private Long assignedTo;
    private LocalDate dueDate;
    private String priority;
    private String status;
}
