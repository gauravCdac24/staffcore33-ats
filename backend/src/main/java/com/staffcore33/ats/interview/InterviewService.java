package com.staffcore33.ats.interview;

import com.staffcore33.ats.audit.AuditService;
import com.staffcore33.ats.candidate.CandidateService;
import com.staffcore33.ats.common.ResourceNotFoundException;
import com.staffcore33.ats.job.Job;
import com.staffcore33.ats.job.JobService;
import com.staffcore33.ats.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class InterviewService {

    private final InterviewRepository interviewRepository;
    private final CandidateService candidateService;
    private final JobService jobService;
    private final AuditService auditService;

    @Transactional(readOnly = true)
    public List<InterviewResponse> list(Long jobId, Long candidateId, Long clientId, String status, Long recruiterId) {
        return interviewRepository.findFiltered(jobId, candidateId, clientId, emptyToNull(status), recruiterId)
                .stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public InterviewResponse get(Long id) {
        return toResponse(find(id));
    }

    @Transactional
    public InterviewResponse create(InterviewRequest request) {
        candidateService.findActive(request.getCandidateId());
        Job job = jobService.findActive(request.getJobId());
        Interview interview = map(new Interview(), request);
        interview.setClientId(job.getClientId());
        interview.setStatus(request.getStatus() == null || request.getStatus().isBlank() ? "SCHEDULED" : request.getStatus());
        if (interview.getDurationMinutes() == null) {
            interview.setDurationMinutes(60);
        }
        if (interview.getRecruiterId() == null) {
            interview.setRecruiterId(SecurityUtils.currentUserId());
        }
        interview = interviewRepository.save(interview);
        auditService.log("INTERVIEW_CREATED", "INTERVIEW", interview.getId(), interview.getRoundName());
        return toResponse(interview);
    }

    @Transactional
    public InterviewResponse update(Long id, InterviewRequest request) {
        Interview interview = find(id);
        candidateService.findActive(request.getCandidateId());
        Job job = jobService.findActive(request.getJobId());
        map(interview, request);
        interview.setClientId(job.getClientId());
        if (request.getStatus() != null && !request.getStatus().isBlank()) {
            interview.setStatus(request.getStatus());
        }
        auditService.log("INTERVIEW_UPDATED", "INTERVIEW", id, interview.getStatus());
        return toResponse(interview);
    }

    @Transactional
    public void delete(Long id) {
        Interview interview = find(id);
        interviewRepository.delete(interview);
        auditService.log("INTERVIEW_DELETED", "INTERVIEW", id, null);
    }

    private Interview map(Interview i, InterviewRequest request) {
        i.setCandidateId(request.getCandidateId());
        i.setJobId(request.getJobId());
        i.setRoundName(request.getRoundName());
        i.setInterviewDate(request.getInterviewDate());
        i.setInterviewTime(request.getInterviewTime());
        i.setDurationMinutes(request.getDurationMinutes());
        i.setInterviewType(request.getInterviewType());
        i.setInterviewer(request.getInterviewer());
        i.setRecruiterId(request.getRecruiterId());
        i.setNotes(request.getNotes());
        i.setFeedback(request.getFeedback());
        i.setResult(request.getResult());
        return i;
    }

    private Interview find(Long id) {
        return interviewRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Interview not found"));
    }

    private InterviewResponse toResponse(Interview i) {
        return InterviewResponse.builder()
                .id(i.getId())
                .candidateId(i.getCandidateId())
                .jobId(i.getJobId())
                .clientId(i.getClientId())
                .roundName(i.getRoundName())
                .interviewDate(i.getInterviewDate())
                .interviewTime(i.getInterviewTime())
                .durationMinutes(i.getDurationMinutes())
                .interviewType(i.getInterviewType())
                .interviewer(i.getInterviewer())
                .recruiterId(i.getRecruiterId())
                .notes(i.getNotes())
                .feedback(i.getFeedback())
                .result(i.getResult())
                .status(i.getStatus())
                .createdAt(i.getCreatedAt())
                .updatedAt(i.getUpdatedAt())
                .build();
    }

    private String emptyToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
