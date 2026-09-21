package com.staffcore33.ats.job;

import com.staffcore33.ats.audit.AuditService;
import com.staffcore33.ats.client.ClientService;
import com.staffcore33.ats.common.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class JobService {

    private final JobRepository jobRepository;
    private final ClientService clientService;
    private final AuditService auditService;

    @Transactional(readOnly = true)
    public List<JobResponse> list(String status, Long clientId, Long recruiterId, String priority, String search) {
        return jobRepository.findFiltered(
                        emptyToNull(status), clientId, recruiterId, emptyToNull(priority), emptyToNull(search))
                .stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<JobResponse> listDeleted() {
        return jobRepository.findAllDeleted().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public JobResponse get(Long id) {
        return toResponse(findActive(id));
    }

    @Transactional
    public JobResponse create(JobRequest request) {
        clientService.findActive(request.getClientId());
        Job job = mapRequest(new Job(), request);
        job.setJobCode("TMP-" + UUID.randomUUID());
        job.setStatus(request.getStatus() == null || request.getStatus().isBlank() ? "NEW" : request.getStatus());
        if (job.getOpenings() == null) {
            job.setOpenings(1);
        }
        if (job.getPriority() == null || job.getPriority().isBlank()) {
            job.setPriority("MEDIUM");
        }
        job = jobRepository.saveAndFlush(job);
        job.setJobCode(String.format("SCJ%04d", job.getId()));
        auditService.log("JOB_CREATED", "JOB", job.getId(), job.getJobCode() + " " + job.getTitle());
        return toResponse(job);
    }

    @Transactional
    public JobResponse update(Long id, JobRequest request) {
        Job job = findActive(id);
        clientService.findActive(request.getClientId());
        mapRequest(job, request);
        if (request.getStatus() != null && !request.getStatus().isBlank()) {
            job.setStatus(request.getStatus());
        }
        auditService.log("JOB_UPDATED", "JOB", job.getId(), job.getJobCode());
        return toResponse(job);
    }

    @Transactional
    public void softDelete(Long id) {
        Job job = findActive(id);
        job.setDeletedAt(Instant.now());
        auditService.log("JOB_DELETED", "JOB", job.getId(), job.getJobCode());
    }

    @Transactional
    public JobResponse restore(Long id) {
        Job job = jobRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found"));
        job.setDeletedAt(null);
        auditService.log("JOB_RESTORED", "JOB", job.getId(), job.getJobCode());
        return toResponse(job);
    }

    public Job findActive(Long id) {
        return jobRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found"));
    }

    private Job mapRequest(Job job, JobRequest request) {
        job.setClientId(request.getClientId());
        job.setTitle(request.getTitle().trim());
        job.setDescription(request.getDescription());
        job.setRequiredSkills(request.getRequiredSkills());
        job.setPreferredSkills(request.getPreferredSkills());
        job.setYearsExperience(request.getYearsExperience());
        job.setLocation(request.getLocation());
        job.setWorkMode(request.getWorkMode());
        job.setWorkAuthorization(request.getWorkAuthorization());
        job.setEmploymentType(request.getEmploymentType());
        job.setPayType(request.getPayType());
        job.setPayRate(request.getPayRate());
        job.setBillRate(request.getBillRate());
        job.setContractDuration(request.getContractDuration());
        job.setStartDate(request.getStartDate());
        job.setOpenings(request.getOpenings());
        job.setPriority(request.getPriority());
        job.setAssignedRecruiterId(request.getAssignedRecruiterId());
        job.setAccountManagerId(request.getAccountManagerId());
        job.setDateReceived(request.getDateReceived());
        return job;
    }

    JobResponse toResponse(Job j) {
        return JobResponse.builder()
                .id(j.getId())
                .jobCode(j.getJobCode())
                .clientId(j.getClientId())
                .title(j.getTitle())
                .description(j.getDescription())
                .requiredSkills(j.getRequiredSkills())
                .preferredSkills(j.getPreferredSkills())
                .yearsExperience(j.getYearsExperience())
                .location(j.getLocation())
                .workMode(j.getWorkMode())
                .workAuthorization(j.getWorkAuthorization())
                .employmentType(j.getEmploymentType())
                .payType(j.getPayType())
                .payRate(j.getPayRate())
                .billRate(j.getBillRate())
                .contractDuration(j.getContractDuration())
                .startDate(j.getStartDate())
                .openings(j.getOpenings())
                .priority(j.getPriority())
                .assignedRecruiterId(j.getAssignedRecruiterId())
                .accountManagerId(j.getAccountManagerId())
                .dateReceived(j.getDateReceived())
                .status(j.getStatus())
                .createdAt(j.getCreatedAt())
                .updatedAt(j.getUpdatedAt())
                .build();
    }

    private String emptyToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
