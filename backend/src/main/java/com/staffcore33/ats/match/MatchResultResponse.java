package com.staffcore33.ats.match;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@Builder
public class MatchResultResponse {
    private Long candidateId;
    private String candidateCode;
    private String candidateName;
    private int score;
    private Map<String, Integer> categoryScores;
    private List<String> reasons;
    private List<String> gaps;
}
