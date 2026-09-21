package com.staffcore33.ats.job;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/jobs")
@RequiredArgsConstructor
public class JobController {

    private final JobService jobService;

    @GetMapping
    public List<JobResponse> list(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long clientId,
            @RequestParam(required = false) Long recruiterId,
            @RequestParam(required = false) String priority,
            @RequestParam(required = false) String search) {
        return jobService.list(status, clientId, recruiterId, priority, search);
    }

    @GetMapping("/deleted")
    public List<JobResponse> listDeleted() {
        return jobService.listDeleted();
    }

    @GetMapping("/{id}")
    public JobResponse get(@PathVariable Long id) {
        return jobService.get(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public JobResponse create(@Valid @RequestBody JobRequest request) {
        return jobService.create(request);
    }

    @PutMapping("/{id}")
    public JobResponse update(@PathVariable Long id, @Valid @RequestBody JobRequest request) {
        return jobService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        jobService.softDelete(id);
    }

    @PostMapping("/{id}/restore")
    public JobResponse restore(@PathVariable Long id) {
        return jobService.restore(id);
    }
}
