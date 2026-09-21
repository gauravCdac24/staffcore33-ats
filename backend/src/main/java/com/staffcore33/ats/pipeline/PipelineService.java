package com.staffcore33.ats.pipeline;

import com.staffcore33.ats.audit.AuditService;
import com.staffcore33.ats.candidate.CandidateService;
import com.staffcore33.ats.common.BadRequestException;
import com.staffcore33.ats.common.ResourceNotFoundException;
import com.staffcore33.ats.job.JobService;
import com.staffcore33.ats.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class PipelineService {

    public static final Set<String> VALID_STATUSES = Set.of(
            "SOURCED", "CONTACTED", "INTERESTED", "SCREENING", "SUBMITTED", "CLIENT_REVIEW",
            "INTERVIEW", "SELECTED", "ONBOARDING", "PLACED",
            "NOT_INTERESTED", "REJECTED_BY_RECRUITER", "REJECTED_BY_CLIENT",
            "WITHDRAWN", "POSITION_CLOSED", "DUPLICATE"
    );

    private final JobCandidateRepository jobCandidateRepository;
    private final JobService jobService;
    private final CandidateService candidateService;
    private final AuditService auditService;

    @Transactional(readOnly = true)
    public List<JobCandidateResponse> listByJob(Long jobId) {
        jobService.findActive(jobId);
        return jobCandidateRepository.findByJobIdOrderByUpdatedAtDesc(jobId).stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<JobCandidateResponse> listByCandidate(Long candidateId) {
        candidateService.findActive(candidateId);
        return jobCandidateRepository.findByCandidateIdOrderByUpdatedAtDesc(candidateId).stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public JobCandidateResponse get(Long id) {
        return toResponse(find(id));
    }

    @Transactional
    public JobCandidateResponse add(JobCandidateRequest request) {
        jobService.findActive(request.getJobId());
        candidateService.findActive(request.getCandidateId());
        jobCandidateRepository.findByJobIdAndCandidateId(request.getJobId(), request.getCandidateId())
                .ifPresent(existing -> {
                    throw new BadRequestException("Candidate is already in this job pipeline");
                });
        String status = normalizeStatus(request.getStatus() == null ? "SOURCED" : request.getStatus());
        JobCandidate jc = JobCandidate.builder()
                .jobId(request.getJobId())
                .candidateId(request.getCandidateId())
                .status(status)
                .notes(request.getNotes())
                .createdBy(SecurityUtils.currentUserId())
                .build();
        jc = jobCandidateRepository.save(jc);
        auditService.log("PIPELINE_ADDED", "JOB_CANDIDATE", jc.getId(),
                "job=" + jc.getJobId() + " candidate=" + jc.getCandidateId() + " status=" + jc.getStatus());
        return toResponse(jc);
    }

    @Transactional
    public JobCandidateResponse updateStatus(Long id, PipelineStatusUpdateRequest request) {
        JobCandidate jc = find(id);
        jc.setStatus(normalizeStatus(request.getStatus()));
        if (request.getNotes() != null) {
            jc.setNotes(request.getNotes());
        }
        auditService.log("PIPELINE_STATUS_UPDATED", "JOB_CANDIDATE", jc.getId(), jc.getStatus());
        return toResponse(jc);
    }

    @Transactional
    public void remove(Long id) {
        JobCandidate jc = find(id);
        jobCandidateRepository.delete(jc);
        auditService.log("PIPELINE_REMOVED", "JOB_CANDIDATE", id, null);
    }

    private String normalizeStatus(String status) {
        String normalized = status.trim().toUpperCase(Locale.ROOT).replace(' ', '_').replace('-', '_');
        if (!VALID_STATUSES.contains(normalized)) {
            throw new BadRequestException("Invalid pipeline status: " + status);
        }
        return normalized;
    }

    private JobCandidate find(Long id) {
        return jobCandidateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pipeline entry not found"));
    }

    private JobCandidateResponse toResponse(JobCandidate jc) {
        return JobCandidateResponse.builder()
                .id(jc.getId())
                .jobId(jc.getJobId())
                .candidateId(jc.getCandidateId())
                .status(jc.getStatus())
                .notes(jc.getNotes())
                .createdBy(jc.getCreatedBy())
                .createdAt(jc.getCreatedAt())
                .updatedAt(jc.getUpdatedAt())
                .build();
    }
}
