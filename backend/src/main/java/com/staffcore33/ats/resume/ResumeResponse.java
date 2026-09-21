package com.staffcore33.ats.resume;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class ResumeResponse {
    private Long id;
    private Long candidateId;
    private String versionType;
    private String originalName;
    private String contentType;
    private Long fileSize;
    private Long uploadedBy;
    private Instant createdAt;
}
