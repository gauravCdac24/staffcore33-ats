package com.staffcore33.ats.pipeline;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class PipelineController {

    private final PipelineService pipelineService;

    @GetMapping("/api/jobs/{jobId}/pipeline")
    public List<JobCandidateResponse> listByJob(@PathVariable Long jobId) {
        return pipelineService.listByJob(jobId);
    }

    @GetMapping("/api/candidates/{candidateId}/pipeline")
    public List<JobCandidateResponse> listByCandidate(@PathVariable Long candidateId) {
        return pipelineService.listByCandidate(candidateId);
    }

    @GetMapping("/api/pipeline/{id}")
    public JobCandidateResponse get(@PathVariable Long id) {
        return pipelineService.get(id);
    }

    @PostMapping("/api/pipeline")
    @ResponseStatus(HttpStatus.CREATED)
    public JobCandidateResponse add(@Valid @RequestBody JobCandidateRequest request) {
        return pipelineService.add(request);
    }

    @PatchMapping("/api/pipeline/{id}/status")
    public JobCandidateResponse updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody PipelineStatusUpdateRequest request) {
        return pipelineService.updateStatus(id, request);
    }

    @DeleteMapping("/api/pipeline/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void remove(@PathVariable Long id) {
        pipelineService.remove(id);
    }
}
