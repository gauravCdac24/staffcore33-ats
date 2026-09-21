package com.staffcore33.ats.candidate;

import com.staffcore33.ats.audit.AuditService;
import com.staffcore33.ats.common.ResourceNotFoundException;
import com.staffcore33.ats.tag.TagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CandidateService {

    private final CandidateRepository candidateRepository;
    private final TagRepository tagRepository;
    private final AuditService auditService;

    @Transactional(readOnly = true)
    public List<CandidateResponse> list(String status, Long recruiterId, String city, String state, String skills, String search) {
        return candidateRepository.findFiltered(
                        emptyToNull(status), recruiterId, emptyToNull(city), emptyToNull(state),
                        emptyToNull(skills), emptyToNull(search))
                .stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<CandidateResponse> listDeleted() {
        return candidateRepository.findAllDeleted().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public CandidateResponse get(Long id) {
        return toResponse(findActive(id));
    }

    @Transactional
    public CandidateResponse create(CandidateRequest request) {
        Candidate candidate = mapRequest(new Candidate(), request);
        candidate.setCandidateCode("TMP-" + UUID.randomUUID());
        candidate.setStatus(request.getStatus() == null || request.getStatus().isBlank() ? "NEW" : request.getStatus());
        if (candidate.getWillingToTravel() == null) {
            candidate.setWillingToTravel(false);
        }
        candidate = candidateRepository.saveAndFlush(candidate);
        candidate.setCandidateCode(String.format("SCC%04d", candidate.getId()));
        auditService.log("CANDIDATE_CREATED", "CANDIDATE", candidate.getId(), candidate.getCandidateCode());
        return toResponse(candidate);
    }

    @Transactional
    public CandidateResponse update(Long id, CandidateRequest request) {
        Candidate candidate = findActive(id);
        mapRequest(candidate, request);
        if (request.getStatus() != null && !request.getStatus().isBlank()) {
            candidate.setStatus(request.getStatus());
        }
        auditService.log("CANDIDATE_UPDATED", "CANDIDATE", candidate.getId(), candidate.getCandidateCode());
        return toResponse(candidate);
    }

    @Transactional
    public void softDelete(Long id) {
        Candidate candidate = findActive(id);
        candidate.setDeletedAt(Instant.now());
        auditService.log("CANDIDATE_DELETED", "CANDIDATE", candidate.getId(), candidate.getCandidateCode());
    }

    @Transactional
    public CandidateResponse restore(Long id) {
        Candidate candidate = candidateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Candidate not found"));
        candidate.setDeletedAt(null);
        auditService.log("CANDIDATE_RESTORED", "CANDIDATE", candidate.getId(), candidate.getCandidateCode());
        return toResponse(candidate);
    }

    @Transactional(readOnly = true)
    public DuplicateCheckResponse checkDuplicates(DuplicateCheckRequest request) {
        List<Candidate> matches = candidateRepository.findPotentialDuplicates(
                emptyToNull(request.getEmail()),
                emptyToNull(request.getPhone()),
                emptyToNull(request.getLinkedinUrl()),
                emptyToNull(request.getFirstName()),
                emptyToNull(request.getLastName()),
                emptyToNull(request.getCity()));
        return DuplicateCheckResponse.builder()
                .hasDuplicates(!matches.isEmpty())
                .message(matches.isEmpty() ? null : "Possible duplicate candidate found.")
                .matches(matches.stream().map(this::toResponse).toList())
                .build();
    }

    public Candidate findActive(Long id) {
        return candidateRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("Candidate not found"));
    }

    private Candidate mapRequest(Candidate c, CandidateRequest request) {
        c.setFirstName(request.getFirstName().trim());
        c.setLastName(request.getLastName().trim());
        c.setEmail(request.getEmail());
        c.setPhone(request.getPhone());
        c.setCity(request.getCity());
        c.setState(request.getState());
        c.setZip(request.getZip());
        c.setLinkedinUrl(request.getLinkedinUrl());
        c.setCurrentTitle(request.getCurrentTitle());
        c.setCurrentCompany(request.getCurrentCompany());
        c.setTotalExperienceYears(request.getTotalExperienceYears());
        c.setPrimarySkills(request.getPrimarySkills());
        c.setSecondarySkills(request.getSecondarySkills());
        c.setPreviousEmployers(request.getPreviousEmployers());
        c.setEducation(request.getEducation());
        c.setCertifications(request.getCertifications());
        c.setWorkAuthorization(request.getWorkAuthorization());
        c.setVisaType(request.getVisaType());
        c.setEmploymentPref(request.getEmploymentPref());
        c.setDesiredRate(request.getDesiredRate());
        c.setCurrentRate(request.getCurrentRate());
        c.setAvailability(request.getAvailability());
        c.setNoticePeriod(request.getNoticePeriod());
        c.setRelocationPref(request.getRelocationPref());
        c.setRemotePref(request.getRemotePref());
        c.setWillingToTravel(request.getWillingToTravel());
        c.setRecruiterId(request.getRecruiterId());
        c.setSource(request.getSource());
        c.setNotes(request.getNotes());
        c.setLastContactedAt(request.getLastContactedAt());
        c.setNextFollowUpAt(request.getNextFollowUpAt());
        return c;
    }

    CandidateResponse toResponse(Candidate c) {
        List<String> tags = tagRepository.findNamesByCandidateId(c.getId());
        return CandidateResponse.builder()
                .id(c.getId())
                .candidateCode(c.getCandidateCode())
                .firstName(c.getFirstName())
                .lastName(c.getLastName())
                .email(c.getEmail())
                .phone(c.getPhone())
                .city(c.getCity())
                .state(c.getState())
                .zip(c.getZip())
                .linkedinUrl(c.getLinkedinUrl())
                .currentTitle(c.getCurrentTitle())
                .currentCompany(c.getCurrentCompany())
                .totalExperienceYears(c.getTotalExperienceYears())
                .primarySkills(c.getPrimarySkills())
                .secondarySkills(c.getSecondarySkills())
                .previousEmployers(c.getPreviousEmployers())
                .education(c.getEducation())
                .certifications(c.getCertifications())
                .workAuthorization(c.getWorkAuthorization())
                .visaType(c.getVisaType())
                .employmentPref(c.getEmploymentPref())
                .desiredRate(c.getDesiredRate())
                .currentRate(c.getCurrentRate())
                .availability(c.getAvailability())
                .noticePeriod(c.getNoticePeriod())
                .relocationPref(c.getRelocationPref())
                .remotePref(c.getRemotePref())
                .willingToTravel(c.getWillingToTravel())
                .recruiterId(c.getRecruiterId())
                .source(c.getSource())
                .notes(c.getNotes())
                .status(c.getStatus())
                .lastContactedAt(c.getLastContactedAt())
                .nextFollowUpAt(c.getNextFollowUpAt())
                .createdAt(c.getCreatedAt())
                .updatedAt(c.getUpdatedAt())
                .tags(tags)
                .build();
    }

    private String emptyToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
