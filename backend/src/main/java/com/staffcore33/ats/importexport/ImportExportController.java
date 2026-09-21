package com.staffcore33.ats.importexport;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.Map;

@RestController
@RequestMapping("/api/import-export")
@RequiredArgsConstructor
public class ImportExportController {

    private final ImportExportService importExportService;

    @PostMapping("/candidates/import")
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, Object> importCandidates(@RequestParam("file") MultipartFile file) {
        return importExportService.importCandidates(file);
    }

    @GetMapping("/candidates/export")
    public ResponseEntity<byte[]> exportCandidates() {
        return csvResponse(importExportService.exportCandidatesCsv(), "candidates.csv");
    }

    @GetMapping("/jobs/export")
    public ResponseEntity<byte[]> exportJobs() {
        return csvResponse(importExportService.exportJobsCsv(), "jobs.csv");
    }

    @GetMapping("/submissions/export")
    public ResponseEntity<byte[]> exportSubmissions() {
        return csvResponse(importExportService.exportSubmissionsCsv(), "submissions.csv");
    }

    private ResponseEntity<byte[]> csvResponse(String content, String filename) {
        byte[] bytes = content.getBytes(StandardCharsets.UTF_8);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(new MediaType("text", "csv", StandardCharsets.UTF_8))
                .body(bytes);
    }
}
