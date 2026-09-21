package com.staffcore33.ats.match;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class MatchController {

    private final MatchService matchService;

    @PostMapping("/api/jobs/{id}/match")
    public List<MatchResultResponse> match(
            @PathVariable Long id,
            @RequestParam(required = false) Integer limit) {
        return matchService.matchJob(id, limit);
    }
}
