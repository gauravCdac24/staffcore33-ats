package com.staffcore33.ats.task;

import com.staffcore33.ats.audit.AuditService;
import com.staffcore33.ats.common.ResourceNotFoundException;
import com.staffcore33.ats.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;
    private final AuditService auditService;

    @Transactional(readOnly = true)
    public List<TaskResponse> list(
            Long assignedTo, String status, Long candidateId, Long jobId, Long clientId, String filter) {
        String normalizedFilter = emptyToNull(filter);
        if (normalizedFilter != null) {
            normalizedFilter = normalizedFilter.toLowerCase();
        }
        return taskRepository.findFiltered(
                        assignedTo, emptyToNull(status), candidateId, jobId, clientId,
                        normalizedFilter, LocalDate.now())
                .stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public TaskResponse get(Long id) {
        return toResponse(find(id));
    }

    @Transactional
    public TaskResponse create(TaskRequest request) {
        Task task = map(new Task(), request);
        task.setStatus(request.getStatus() == null || request.getStatus().isBlank() ? "OPEN" : request.getStatus());
        if (task.getPriority() == null || task.getPriority().isBlank()) {
            task.setPriority("MEDIUM");
        }
        if (task.getAssignedTo() == null) {
            task.setAssignedTo(SecurityUtils.currentUserId());
        }
        task.setCreatedBy(SecurityUtils.currentUserId());
        task = taskRepository.save(task);
        auditService.log("TASK_CREATED", "TASK", task.getId(), task.getTitle());
        return toResponse(task);
    }

    @Transactional
    public TaskResponse update(Long id, TaskRequest request) {
        Task task = find(id);
        map(task, request);
        if (request.getStatus() != null && !request.getStatus().isBlank()) {
            task.setStatus(request.getStatus());
        }
        auditService.log("TASK_UPDATED", "TASK", id, task.getTitle());
        return toResponse(task);
    }

    @Transactional
    public void delete(Long id) {
        Task task = find(id);
        taskRepository.delete(task);
        auditService.log("TASK_DELETED", "TASK", id, task.getTitle());
    }

    private Task map(Task task, TaskRequest request) {
        task.setTitle(request.getTitle().trim());
        task.setNotes(request.getNotes());
        task.setCandidateId(request.getCandidateId());
        task.setJobId(request.getJobId());
        task.setClientId(request.getClientId());
        task.setAssignedTo(request.getAssignedTo());
        task.setDueDate(request.getDueDate());
        task.setPriority(request.getPriority());
        return task;
    }

    private Task find(Long id) {
        return taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found"));
    }

    private TaskResponse toResponse(Task t) {
        return TaskResponse.builder()
                .id(t.getId())
                .title(t.getTitle())
                .notes(t.getNotes())
                .candidateId(t.getCandidateId())
                .jobId(t.getJobId())
                .clientId(t.getClientId())
                .assignedTo(t.getAssignedTo())
                .dueDate(t.getDueDate())
                .priority(t.getPriority())
                .status(t.getStatus())
                .createdBy(t.getCreatedBy())
                .createdAt(t.getCreatedAt())
                .updatedAt(t.getUpdatedAt())
                .build();
    }

    private String emptyToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
