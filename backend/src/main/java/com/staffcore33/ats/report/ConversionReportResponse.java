package com.staffcore33.ats.report;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
public class ConversionReportResponse {
    private Map<String, Long> stageCounts;
    private Map<String, Double> conversionRates;
}
