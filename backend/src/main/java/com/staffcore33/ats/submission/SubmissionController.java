package com.staffcore33.ats.submission;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/submissions")
@RequiredArgsConstructor
public class SubmissionController {

    private final SubmissionService submissionService;

    @GetMapping
    public List<SubmissionResponse> list(
            @RequestParam(required = false) Long jobId,
            @RequestParam(required = false) Long candidateId,
            @RequestParam(required = false) Long clientId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long submittedBy) {
        return submissionService.list(jobId, candidateId, clientId, status, submittedBy);
    }

    @GetMapping("/{id}")
    public SubmissionResponse get(@PathVariable Long id) {
        return submissionService.get(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SubmissionResponse create(@Valid @RequestBody SubmissionRequest request) {
        return submissionService.create(request);
    }

    @PutMapping("/{id}")
    public SubmissionResponse update(@PathVariable Long id, @Valid @RequestBody SubmissionRequest request) {
        return submissionService.update(id, request);
    }

    @PatchMapping("/{id}/status")
    public SubmissionResponse updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody SubmissionStatusUpdateRequest request) {
        return submissionService.updateStatus(id, request);
    }
}
