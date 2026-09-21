package com.staffcore33.ats.report;

import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @GetMapping("/recruiters")
    public List<RecruiterReportResponse> recruiters(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) Long recruiterId) {
        return reportService.recruiterReport(from, to, recruiterId);
    }

    @GetMapping("/jobs")
    public List<JobReportItem> jobs(
            @RequestParam(required = false) Long clientId,
            @RequestParam(required = false) String status) {
        return reportService.jobReport(clientId, status);
    }

    @GetMapping("/clients")
    public List<ClientReportItem> clients(@RequestParam(required = false) Long clientId) {
        return reportService.clientReport(clientId);
    }

    @GetMapping("/conversion")
    public ConversionReportResponse conversion() {
        return reportService.conversionReport();
    }
}
