package com.staffcore33.ats.report;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class JobReportItem {
    private Long jobId;
    private String jobCode;
    private String title;
    private String status;
    private long candidatesInPipeline;
    private long submissions;
    private long interviews;
    private long placements;
    private Long daysOpen;
}
