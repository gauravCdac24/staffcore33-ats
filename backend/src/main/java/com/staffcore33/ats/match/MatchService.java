package com.staffcore33.ats.match;

import com.staffcore33.ats.candidate.Candidate;
import com.staffcore33.ats.candidate.CandidateRepository;
import com.staffcore33.ats.job.Job;
import com.staffcore33.ats.job.JobService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MatchService {

    private final JobService jobService;
    private final CandidateRepository candidateRepository;

    @Transactional(readOnly = true)
    public List<MatchResultResponse> matchJob(Long jobId, Integer limit) {
        Job job = jobService.findActive(jobId);
        int max = limit == null || limit < 1 ? 50 : Math.min(limit, 200);
        return candidateRepository.findByDeletedAtIsNull().stream()
                .map(c -> score(job, c))
                .sorted(Comparator.comparingInt(MatchResultResponse::getScore).reversed())
                .limit(max)
                .toList();
    }

    private MatchResultResponse score(Job job, Candidate candidate) {
        List<String> reasons = new ArrayList<>();
        List<String> gaps = new ArrayList<>();
        Map<String, Integer> categoryScores = new LinkedHashMap<>();

        int skillsScore = scoreSkills(job, candidate, reasons, gaps);
        categoryScores.put("skills", skillsScore);

        int experienceScore = scoreExperience(job, candidate, reasons, gaps);
        categoryScores.put("experience", experienceScore);

        int locationScore = scoreLocation(job, candidate, reasons, gaps);
        categoryScores.put("location", locationScore);

        int workAuthScore = scoreWorkAuth(job, candidate, reasons, gaps);
        categoryScores.put("workAuthorization", workAuthScore);

        int rateScore = scoreRate(job, candidate, reasons, gaps);
        categoryScores.put("rate", rateScore);

        // Weighted: skills 40, experience 20, location 15, work auth 15, rate 10
        int total = (int) Math.round(
                skillsScore * 0.40
                        + experienceScore * 0.20
                        + locationScore * 0.15
                        + workAuthScore * 0.15
                        + rateScore * 0.10);

        return MatchResultResponse.builder()
                .candidateId(candidate.getId())
                .candidateCode(candidate.getCandidateCode())
                .candidateName(candidate.getFirstName() + " " + candidate.getLastName())
                .score(Math.max(0, Math.min(100, total)))
                .categoryScores(categoryScores)
                .reasons(reasons)
                .gaps(gaps)
                .build();
    }

    private int scoreSkills(Job job, Candidate candidate, List<String> reasons, List<String> gaps) {
        Set<String> required = tokenize(job.getRequiredSkills());
        Set<String> preferred = tokenize(job.getPreferredSkills());
        Set<String> candidateSkills = new HashSet<>();
        candidateSkills.addAll(tokenize(candidate.getPrimarySkills()));
        candidateSkills.addAll(tokenize(candidate.getSecondarySkills()));

        if (required.isEmpty() && preferred.isEmpty()) {
            reasons.add("No specific skills listed on the job");
            return 70;
        }

        int matchedRequired = 0;
        for (String skill : required) {
            if (containsSkill(candidateSkills, skill)) {
                matchedRequired++;
                reasons.add("Has required skill: " + skill);
            } else {
                gaps.add("Missing required skill: " + skill);
            }
        }
        int matchedPreferred = 0;
        for (String skill : preferred) {
            if (containsSkill(candidateSkills, skill)) {
                matchedPreferred++;
                reasons.add("Has preferred skill: " + skill);
            } else {
                gaps.add("Preferred skill not found: " + skill);
            }
        }

        double requiredRatio = required.isEmpty() ? 1.0 : (double) matchedRequired / required.size();
        double preferredRatio = preferred.isEmpty() ? 1.0 : (double) matchedPreferred / preferred.size();
        return (int) Math.round((requiredRatio * 0.75 + preferredRatio * 0.25) * 100);
    }

    private int scoreExperience(Job job, Candidate candidate, List<String> reasons, List<String> gaps) {
        if (job.getYearsExperience() == null) {
            return 80;
        }
        BigDecimal years = candidate.getTotalExperienceYears();
        if (years == null) {
            gaps.add("Experience years not provided");
            return 40;
        }
        double required = job.getYearsExperience();
        double actual = years.doubleValue();
        if (actual >= required) {
            reasons.add(actual + " years experience meets requirement of " + required);
            return 100;
        }
        if (actual >= required * 0.8) {
            reasons.add(actual + " years experience close to required " + required);
            return 75;
        }
        gaps.add(actual + " years experience below required " + required);
        return (int) Math.max(20, Math.round((actual / required) * 100));
    }

    private int scoreLocation(Job job, Candidate candidate, List<String> reasons, List<String> gaps) {
        String jobLoc = normalize(job.getLocation());
        String workMode = normalize(job.getWorkMode());
        if (workMode.contains("remote")) {
            reasons.add("Job is remote");
            return 100;
        }
        if (jobLoc.isEmpty()) {
            return 70;
        }
        String candCity = normalize(candidate.getCity());
        String candState = normalize(candidate.getState());
        String candLoc = (candCity + " " + candState).trim();
        if (!candLoc.isBlank() && (jobLoc.contains(candCity) || jobLoc.contains(candState) || candLoc.contains(jobLoc))) {
            reasons.add("Location aligns with " + job.getLocation());
            return 100;
        }
        String remotePref = normalize(candidate.getRemotePref());
        if (remotePref.contains("remote") || remotePref.contains("yes")) {
            reasons.add("Candidate open to remote");
            return 70;
        }
        String relocation = normalize(candidate.getRelocationPref());
        if (relocation.contains("yes") || relocation.contains("open")) {
            reasons.add("Candidate open to relocation");
            return 60;
        }
        gaps.add("Location may not match " + job.getLocation());
        return 35;
    }

    private int scoreWorkAuth(Job job, Candidate candidate, List<String> reasons, List<String> gaps) {
        String required = normalize(job.getWorkAuthorization());
        String actual = normalize(candidate.getWorkAuthorization());
        if (required.isEmpty()) {
            return 80;
        }
        if (actual.isEmpty()) {
            gaps.add("Work authorization not provided");
            return 40;
        }
        if (actual.contains(required) || required.contains(actual)
                || (required.contains("usc") && (actual.contains("citizen") || actual.contains("usc")))
                || (required.contains("gc") && (actual.contains("green") || actual.contains("gc")))
                || actual.contains("citizen")) {
            reasons.add("Meets work authorization: " + candidate.getWorkAuthorization());
            return 100;
        }
        gaps.add("Work authorization may not match (" + candidate.getWorkAuthorization() + " vs " + job.getWorkAuthorization() + ")");
        return 30;
    }

    private int scoreRate(Job job, Candidate candidate, List<String> reasons, List<String> gaps) {
        BigDecimal payRate = job.getPayRate();
        BigDecimal desired = candidate.getDesiredRate() != null ? candidate.getDesiredRate() : candidate.getCurrentRate();
        if (payRate == null || desired == null) {
            return 70;
        }
        int cmp = desired.compareTo(payRate);
        if (cmp <= 0) {
            reasons.add("Desired rate " + desired + " within/under pay rate " + payRate);
            return 100;
        }
        BigDecimal diff = desired.subtract(payRate);
        BigDecimal pct = diff.divide(payRate, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100));
        if (pct.doubleValue() <= 10) {
            reasons.add("Desired rate slightly above target (+" + diff + ")");
            return 75;
        }
        gaps.add("Rate is $" + diff + "/hr above target");
        return Math.max(20, 100 - (int) Math.round(pct.doubleValue() * 2));
    }

    private Set<String> tokenize(String text) {
        if (text == null || text.isBlank()) {
            return Set.of();
        }
        return Arrays.stream(text.split("[,;/|\\n]+"))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(s -> s.toLowerCase(Locale.ROOT))
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private boolean containsSkill(Set<String> skills, String skill) {
        String s = skill.toLowerCase(Locale.ROOT);
        return skills.stream().anyMatch(cs -> cs.equals(s) || cs.contains(s) || s.contains(cs));
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }
}
