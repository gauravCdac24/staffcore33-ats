package com.staffcore33.ats.dashboard;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
public class DashboardSummaryResponse {
    private Map<String, Long> jobs;
    private Map<String, Long> candidates;
    private Map<String, Long> pipeline;
    private Map<String, Long> tasks;
    private long recentSubmissions;
    private long totalActivities;
}
