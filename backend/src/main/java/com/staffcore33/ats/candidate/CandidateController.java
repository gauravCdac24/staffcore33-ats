package com.staffcore33.ats.candidate;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/candidates")
@RequiredArgsConstructor
public class CandidateController {

    private final CandidateService candidateService;

    @GetMapping
    public List<CandidateResponse> list(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long recruiterId,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String state,
            @RequestParam(required = false) String skills,
            @RequestParam(required = false) String search) {
        return candidateService.list(status, recruiterId, city, state, skills, search);
    }

    @GetMapping("/deleted")
    public List<CandidateResponse> listDeleted() {
        return candidateService.listDeleted();
    }

    @GetMapping("/{id}")
    public CandidateResponse get(@PathVariable Long id) {
        return candidateService.get(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CandidateResponse create(@Valid @RequestBody CandidateRequest request) {
        return candidateService.create(request);
    }

    @PutMapping("/{id}")
    public CandidateResponse update(@PathVariable Long id, @Valid @RequestBody CandidateRequest request) {
        return candidateService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        candidateService.softDelete(id);
    }

    @PostMapping("/{id}/restore")
    public CandidateResponse restore(@PathVariable Long id) {
        return candidateService.restore(id);
    }

    @PostMapping("/check-duplicates")
    public DuplicateCheckResponse checkDuplicates(@RequestBody DuplicateCheckRequest request) {
        return candidateService.checkDuplicates(request);
    }
}
