package com.staffcore33.ats.interview;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/interviews")
@RequiredArgsConstructor
public class InterviewController {

    private final InterviewService interviewService;

    @GetMapping
    public List<InterviewResponse> list(
            @RequestParam(required = false) Long jobId,
            @RequestParam(required = false) Long candidateId,
            @RequestParam(required = false) Long clientId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long recruiterId) {
        return interviewService.list(jobId, candidateId, clientId, status, recruiterId);
    }

    @GetMapping("/{id}")
    public InterviewResponse get(@PathVariable Long id) {
        return interviewService.get(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public InterviewResponse create(@Valid @RequestBody InterviewRequest request) {
        return interviewService.create(request);
    }

    @PutMapping("/{id}")
    public InterviewResponse update(@PathVariable Long id, @Valid @RequestBody InterviewRequest request) {
        return interviewService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        interviewService.delete(id);
    }
}
