package com.staffcore33.ats.job;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Data
@Builder
public class JobResponse {
    private Long id;
    private String jobCode;
    private Long clientId;
    private String title;
    private String description;
    private String requiredSkills;
    private String preferredSkills;
    private Integer yearsExperience;
    private String location;
    private String workMode;
    private String workAuthorization;
    private String employmentType;
    private String payType;
    private BigDecimal payRate;
    private BigDecimal billRate;
    private String contractDuration;
    private LocalDate startDate;
    private Integer openings;
    private String priority;
    private Long assignedRecruiterId;
    private Long accountManagerId;
    private LocalDate dateReceived;
    private String status;
    private Instant createdAt;
    private Instant updatedAt;
}
