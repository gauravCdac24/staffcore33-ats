package com.staffcore33.ats.activity;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/activities")
@RequiredArgsConstructor
public class ActivityController {

    private final ActivityService activityService;

    @GetMapping
    public List<ActivityResponse> list(
            @RequestParam(required = false) Long candidateId,
            @RequestParam(required = false) Long jobId,
            @RequestParam(required = false) Long clientId,
            @RequestParam(required = false) String activityType) {
        return activityService.list(candidateId, jobId, clientId, activityType);
    }

    @GetMapping("/recent")
    public List<ActivityResponse> recent() {
        return activityService.recent();
    }

    @GetMapping("/{id}")
    public ActivityResponse get(@PathVariable Long id) {
        return activityService.get(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ActivityResponse create(@Valid @RequestBody ActivityRequest request) {
        return activityService.create(request);
    }

    @PutMapping("/{id}")
    public ActivityResponse update(@PathVariable Long id, @Valid @RequestBody ActivityRequest request) {
        return activityService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        activityService.delete(id);
    }
}
