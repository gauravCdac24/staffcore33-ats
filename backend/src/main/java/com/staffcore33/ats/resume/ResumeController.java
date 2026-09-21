package com.staffcore33.ats.resume;

import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class ResumeController {

    private final ResumeService resumeService;

    @GetMapping("/api/candidates/{candidateId}/resumes")
    public List<ResumeResponse> list(@PathVariable Long candidateId) {
        return resumeService.listByCandidate(candidateId);
    }

    @PostMapping("/api/candidates/{candidateId}/resumes")
    @ResponseStatus(HttpStatus.CREATED)
    public ResumeResponse upload(
            @PathVariable Long candidateId,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "versionType", required = false) String versionType) {
        return resumeService.upload(candidateId, file, versionType);
    }

    @GetMapping("/api/resumes/{id}")
    public ResumeResponse get(@PathVariable Long id) {
        return resumeService.get(id);
    }

    @GetMapping("/api/resumes/{id}/download")
    public ResponseEntity<Resource> download(@PathVariable Long id) {
        ResumeService.ResumeFile file = resumeService.download(id);
        MediaType mediaType = MediaType.APPLICATION_OCTET_STREAM;
        if (file.resume().getContentType() != null) {
            try {
                mediaType = MediaType.parseMediaType(file.resume().getContentType());
            } catch (Exception ignored) {
            }
        }
        return ResponseEntity.ok()
                .contentType(mediaType)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + file.resume().getOriginalName() + "\"")
                .body(file.resource());
    }

    @DeleteMapping("/api/resumes/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        resumeService.delete(id);
    }
}
