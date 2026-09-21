package com.staffcore33.ats.job;

import com.staffcore33.ats.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "jobs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Job extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "job_code", nullable = false, unique = true)
    private String jobCode;

    @Column(name = "client_id", nullable = false)
    private Long clientId;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "required_skills", columnDefinition = "TEXT")
    private String requiredSkills;

    @Column(name = "preferred_skills", columnDefinition = "TEXT")
    private String preferredSkills;

    @Column(name = "years_experience")
    private Integer yearsExperience;

    private String location;

    @Column(name = "work_mode")
    private String workMode;

    @Column(name = "work_authorization")
    private String workAuthorization;

    @Column(name = "employment_type")
    private String employmentType;

    @Column(name = "pay_type")
    private String payType;

    @Column(name = "pay_rate", precision = 12, scale = 2)
    private BigDecimal payRate;

    @Column(name = "bill_rate", precision = 12, scale = 2)
    private BigDecimal billRate;

    @Column(name = "contract_duration")
    private String contractDuration;

    @Column(name = "start_date")
    private LocalDate startDate;

    private Integer openings;

    private String priority;

    @Column(name = "assigned_recruiter_id")
    private Long assignedRecruiterId;

    @Column(name = "account_manager_id")
    private Long accountManagerId;

    @Column(name = "date_received")
    private LocalDate dateReceived;

    @Column(nullable = false)
    private String status = "NEW";

    @Column(name = "deleted_at")
    private Instant deletedAt;

    public boolean isDeleted() {
        return deletedAt != null;
    }
}
