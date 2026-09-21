package com.staffcore33.ats.candidate;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

@Data
public class CandidateRequest {

    @NotBlank
    private String firstName;

    @NotBlank
    private String lastName;

    private String email;
    private String phone;
    private String city;
    private String state;
    private String zip;
    private String linkedinUrl;
    private String currentTitle;
    private String currentCompany;
    private BigDecimal totalExperienceYears;
    private String primarySkills;
    private String secondarySkills;
    private String previousEmployers;
    private String education;
    private String certifications;
    private String workAuthorization;
    private String visaType;
    private String employmentPref;
    private BigDecimal desiredRate;
    private BigDecimal currentRate;
    private String availability;
    private String noticePeriod;
    private String relocationPref;
    private String remotePref;
    private Boolean willingToTravel;
    private Long recruiterId;
    private String source;
    private String notes;
    private String status;
    private Instant lastContactedAt;
    private Instant nextFollowUpAt;
}
