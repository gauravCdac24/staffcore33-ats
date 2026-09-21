package com.staffcore33.ats.report;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RecruiterReportResponse {
    private Long recruiterId;
    private String recruiterName;
    private long candidatesAdded;
    private long candidatesContacted;
    private long candidatesScreened;
    private long candidatesSubmitted;
    private long interviews;
    private long placements;
}
