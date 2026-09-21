package com.staffcore33.ats.activity;

import com.staffcore33.ats.audit.AuditService;
import com.staffcore33.ats.common.BadRequestException;
import com.staffcore33.ats.common.ResourceNotFoundException;
import com.staffcore33.ats.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ActivityService {

    private final ActivityRepository activityRepository;
    private final AuditService auditService;

    @Transactional(readOnly = true)
    public List<ActivityResponse> list(Long candidateId, Long jobId, Long clientId, String activityType) {
        return activityRepository.findFiltered(candidateId, jobId, clientId, emptyToNull(activityType))
                .stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<ActivityResponse> recent() {
        return activityRepository.findTop20ByOrderByActivityAtDesc().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public ActivityResponse get(Long id) {
        return toResponse(find(id));
    }

    @Transactional
    public ActivityResponse create(ActivityRequest request) {
        if (request.getCandidateId() == null && request.getJobId() == null && request.getClientId() == null) {
            throw new BadRequestException("Activity must be linked to a candidate, job, or client");
        }
        Activity activity = Activity.builder()
                .activityType(request.getActivityType().trim())
                .notes(request.getNotes())
                .activityAt(request.getActivityAt() != null ? request.getActivityAt() : Instant.now())
                .userId(SecurityUtils.currentUserId())
                .candidateId(request.getCandidateId())
                .jobId(request.getJobId())
                .clientId(request.getClientId())
                .build();
        activity = activityRepository.save(activity);
        auditService.log("ACTIVITY_CREATED", "ACTIVITY", activity.getId(), activity.getActivityType());
        return toResponse(activity);
    }

    @Transactional
    public ActivityResponse update(Long id, ActivityRequest request) {
        Activity activity = find(id);
        activity.setActivityType(request.getActivityType().trim());
        activity.setNotes(request.getNotes());
        if (request.getActivityAt() != null) {
            activity.setActivityAt(request.getActivityAt());
        }
        activity.setCandidateId(request.getCandidateId());
        activity.setJobId(request.getJobId());
        activity.setClientId(request.getClientId());
        auditService.log("ACTIVITY_UPDATED", "ACTIVITY", id, activity.getActivityType());
        return toResponse(activity);
    }

    @Transactional
    public void delete(Long id) {
        Activity activity = find(id);
        activityRepository.delete(activity);
        auditService.log("ACTIVITY_DELETED", "ACTIVITY", id, null);
    }

    private Activity find(Long id) {
        return activityRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Activity not found"));
    }

    private ActivityResponse toResponse(Activity a) {
        return ActivityResponse.builder()
                .id(a.getId())
                .activityType(a.getActivityType())
                .notes(a.getNotes())
                .activityAt(a.getActivityAt())
                .userId(a.getUserId())
                .candidateId(a.getCandidateId())
                .jobId(a.getJobId())
                .clientId(a.getClientId())
                .createdAt(a.getCreatedAt())
                .build();
    }

    private String emptyToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
