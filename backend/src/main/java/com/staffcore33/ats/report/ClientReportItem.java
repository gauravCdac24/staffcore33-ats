package com.staffcore33.ats.report;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ClientReportItem {
    private Long clientId;
    private String companyName;
    private long jobsReceived;
    private long candidatesSubmitted;
    private long interviews;
    private long placements;
}
