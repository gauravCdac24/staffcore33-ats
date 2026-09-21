package com.staffcore33.ats.resume;

import com.staffcore33.ats.audit.AuditService;
import com.staffcore33.ats.candidate.CandidateService;
import com.staffcore33.ats.common.ResourceNotFoundException;
import com.staffcore33.ats.file.FileStorageService;
import com.staffcore33.ats.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ResumeService {

    private final ResumeRepository resumeRepository;
    private final CandidateService candidateService;
    private final FileStorageService fileStorageService;
    private final AuditService auditService;

    @Transactional(readOnly = true)
    public List<ResumeResponse> listByCandidate(Long candidateId) {
        candidateService.findActive(candidateId);
        return resumeRepository.findByCandidateIdOrderByCreatedAtDesc(candidateId)
                .stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public ResumeResponse get(Long id) {
        return toResponse(find(id));
    }

    @Transactional
    public ResumeResponse upload(Long candidateId, MultipartFile file, String versionType) {
        candidateService.findActive(candidateId);
        String stored = fileStorageService.store(file, "resumes/" + candidateId);
        Resume resume = Resume.builder()
                .candidateId(candidateId)
                .versionType(versionType == null || versionType.isBlank() ? "ORIGINAL" : versionType)
                .originalName(file.getOriginalFilename())
                .storedName(stored)
                .contentType(file.getContentType())
                .fileSize(file.getSize())
                .uploadedBy(SecurityUtils.currentUserId())
                .build();
        resume = resumeRepository.save(resume);
        auditService.log("RESUME_UPLOADED", "RESUME", resume.getId(), resume.getOriginalName());
        return toResponse(resume);
    }

    @Transactional(readOnly = true)
    public ResumeFile download(Long id) {
        Resume resume = find(id);
        Resource resource = fileStorageService.loadAsResource(resume.getStoredName());
        return new ResumeFile(resume, resource);
    }

    @Transactional
    public void delete(Long id) {
        Resume resume = find(id);
        fileStorageService.delete(resume.getStoredName());
        resumeRepository.delete(resume);
        auditService.log("RESUME_DELETED", "RESUME", id, resume.getOriginalName());
    }

    public Resume find(Long id) {
        return resumeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Resume not found"));
    }

    private ResumeResponse toResponse(Resume r) {
        return ResumeResponse.builder()
                .id(r.getId())
                .candidateId(r.getCandidateId())
                .versionType(r.getVersionType())
                .originalName(r.getOriginalName())
                .contentType(r.getContentType())
                .fileSize(r.getFileSize())
                .uploadedBy(r.getUploadedBy())
                .createdAt(r.getCreatedAt())
                .build();
    }

    public record ResumeFile(Resume resume, Resource resource) {}
}
