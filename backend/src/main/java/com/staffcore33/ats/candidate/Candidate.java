package com.staffcore33.ats.candidate;

import com.staffcore33.ats.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "candidates")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Candidate extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "candidate_code", nullable = false, unique = true)
    private String candidateCode;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    private String email;
    private String phone;
    private String city;
    private String state;
    private String zip;

    @Column(name = "linkedin_url")
    private String linkedinUrl;

    @Column(name = "current_title")
    private String currentTitle;

    @Column(name = "current_company")
    private String currentCompany;

    @Column(name = "total_experience_years", precision = 5, scale = 1)
    private BigDecimal totalExperienceYears;

    @Column(name = "primary_skills", columnDefinition = "TEXT")
    private String primarySkills;

    @Column(name = "secondary_skills", columnDefinition = "TEXT")
    private String secondarySkills;

    @Column(name = "previous_employers", columnDefinition = "TEXT")
    private String previousEmployers;

    @Column(columnDefinition = "TEXT")
    private String education;

    @Column(columnDefinition = "TEXT")
    private String certifications;

    @Column(name = "work_authorization")
    private String workAuthorization;

    @Column(name = "visa_type")
    private String visaType;

    @Column(name = "employment_pref")
    private String employmentPref;

    @Column(name = "desired_rate", precision = 12, scale = 2)
    private BigDecimal desiredRate;

    @Column(name = "current_rate", precision = 12, scale = 2)
    private BigDecimal currentRate;

    private String availability;

    @Column(name = "notice_period")
    private String noticePeriod;

    @Column(name = "relocation_pref")
    private String relocationPref;

    @Column(name = "remote_pref")
    private String remotePref;

    @Column(name = "willing_to_travel")
    private Boolean willingToTravel;

    @Column(name = "recruiter_id")
    private Long recruiterId;

    private String source;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(nullable = false)
    private String status = "NEW";

    @Column(name = "last_contacted_at")
    private Instant lastContactedAt;

    @Column(name = "next_follow_up_at")
    private Instant nextFollowUpAt;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    public boolean isDeleted() {
        return deletedAt != null;
    }
}
