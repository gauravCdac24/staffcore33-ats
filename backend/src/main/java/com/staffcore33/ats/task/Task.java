package com.staffcore33.ats.task;

import com.staffcore33.ats.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "tasks")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Task extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "candidate_id")
    private Long candidateId;

    @Column(name = "job_id")
    private Long jobId;

    @Column(name = "client_id")
    private Long clientId;

    @Column(name = "assigned_to")
    private Long assignedTo;

    @Column(name = "due_date")
    private LocalDate dueDate;

    private String priority;

    @Column(nullable = false)
    private String status = "OPEN";

    @Column(name = "created_by")
    private Long createdBy;
}
