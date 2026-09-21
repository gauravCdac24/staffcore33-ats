package com.staffcore33.ats.submission;

import com.staffcore33.ats.audit.AuditService;
import com.staffcore33.ats.candidate.CandidateService;
import com.staffcore33.ats.common.BadRequestException;
import com.staffcore33.ats.common.ResourceNotFoundException;
import com.staffcore33.ats.job.Job;
import com.staffcore33.ats.job.JobService;
import com.staffcore33.ats.resume.ResumeService;
import com.staffcore33.ats.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SubmissionService {

    private final SubmissionRepository submissionRepository;
    private final CandidateService candidateService;
    private final JobService jobService;
    private final ResumeService resumeService;
    private final AuditService auditService;

    @Transactional(readOnly = true)
    public List<SubmissionResponse> list(Long jobId, Long candidateId, Long clientId, String status, Long submittedBy) {
        return submissionRepository.findFiltered(jobId, candidateId, clientId, emptyToNull(status), submittedBy)
                .stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public SubmissionResponse get(Long id) {
        return toResponse(find(id));
    }

    @Transactional
    public SubmissionResponse create(SubmissionRequest request) {
        candidateService.findActive(request.getCandidateId());
        Job job = jobService.findActive(request.getJobId());
        if (request.getResumeId() != null) {
            resumeService.find(request.getResumeId());
        }

        var existing = submissionRepository.findFirstByCandidateIdAndJobIdOrderBySubmissionDateDesc(
                request.getCandidateId(), request.getJobId());
        if (existing.isPresent() && !Boolean.TRUE.equals(request.getForce())) {
            throw new BadRequestException(
                    "This candidate was already submitted to this job on " + existing.get().getSubmissionDate() + ".");
        }

        Submission submission = Submission.builder()
                .candidateId(request.getCandidateId())
                .jobId(request.getJobId())
                .clientId(job.getClientId())
                .resumeId(request.getResumeId())
                .submittedBy(SecurityUtils.currentUserId())
                .submissionDate(request.getSubmissionDate() != null ? request.getSubmissionDate() : LocalDate.now())
                .submittedRate(request.getSubmittedRate())
                .billRate(request.getBillRate() != null ? request.getBillRate() : job.getBillRate())
                .notes(request.getNotes())
                .status(request.getStatus() == null || request.getStatus().isBlank() ? "SUBMITTED" : request.getStatus())
                .build();
        submission = submissionRepository.save(submission);
        auditService.log("SUBMISSION_CREATED", "SUBMISSION", submission.getId(),
                "candidate=" + submission.getCandidateId() + " job=" + submission.getJobId());
        return toResponse(submission);
    }

    @Transactional
    public SubmissionResponse updateStatus(Long id, SubmissionStatusUpdateRequest request) {
        Submission submission = find(id);
        submission.setStatus(request.getStatus().trim());
        if (request.getNotes() != null) {
            submission.setNotes(request.getNotes());
        }
        auditService.log("SUBMISSION_STATUS_UPDATED", "SUBMISSION", id, submission.getStatus());
        return toResponse(submission);
    }

    @Transactional
    public SubmissionResponse update(Long id, SubmissionRequest request) {
        Submission submission = find(id);
        if (request.getResumeId() != null) {
            resumeService.find(request.getResumeId());
            submission.setResumeId(request.getResumeId());
        }
        if (request.getSubmissionDate() != null) {
            submission.setSubmissionDate(request.getSubmissionDate());
        }
        if (request.getSubmittedRate() != null) {
            submission.setSubmittedRate(request.getSubmittedRate());
        }
        if (request.getBillRate() != null) {
            submission.setBillRate(request.getBillRate());
        }
        if (request.getNotes() != null) {
            submission.setNotes(request.getNotes());
        }
        if (request.getStatus() != null && !request.getStatus().isBlank()) {
            submission.setStatus(request.getStatus());
        }
        auditService.log("SUBMISSION_UPDATED", "SUBMISSION", id, null);
        return toResponse(submission);
    }

    private Submission find(Long id) {
        return submissionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Submission not found"));
    }

    private SubmissionResponse toResponse(Submission s) {
        return SubmissionResponse.builder()
                .id(s.getId())
                .candidateId(s.getCandidateId())
                .jobId(s.getJobId())
                .clientId(s.getClientId())
                .resumeId(s.getResumeId())
                .submittedBy(s.getSubmittedBy())
                .submissionDate(s.getSubmissionDate())
                .submittedRate(s.getSubmittedRate())
                .billRate(s.getBillRate())
                .notes(s.getNotes())
                .status(s.getStatus())
                .createdAt(s.getCreatedAt())
                .updatedAt(s.getUpdatedAt())
                .build();
    }

    private String emptyToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
