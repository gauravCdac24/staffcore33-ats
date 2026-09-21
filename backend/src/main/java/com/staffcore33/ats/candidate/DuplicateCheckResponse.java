package com.staffcore33.ats.candidate;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class DuplicateCheckResponse {
    private boolean hasDuplicates;
    private String message;
    private List<CandidateResponse> matches;
}
