package com.staffcore33.ats.report;

import com.staffcore33.ats.candidate.CandidateRepository;
import com.staffcore33.ats.client.Client;
import com.staffcore33.ats.client.ClientRepository;
import com.staffcore33.ats.interview.InterviewRepository;
import com.staffcore33.ats.job.Job;
import com.staffcore33.ats.job.JobRepository;
import com.staffcore33.ats.pipeline.JobCandidateRepository;
import com.staffcore33.ats.submission.SubmissionRepository;
import com.staffcore33.ats.user.User;
import com.staffcore33.ats.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final UserRepository userRepository;
    private final CandidateRepository candidateRepository;
    private final SubmissionRepository submissionRepository;
    private final InterviewRepository interviewRepository;
    private final JobRepository jobRepository;
    private final ClientRepository clientRepository;
    private final JobCandidateRepository jobCandidateRepository;

    @Transactional(readOnly = true)
    public List<RecruiterReportResponse> recruiterReport(LocalDate from, LocalDate to, Long recruiterId) {
        Instant fromInstant = toInstantStart(from);
        Instant toInstant = toInstantEnd(to);
        List<User> recruiters = userRepository.findAllActive().stream()
                .filter(u -> recruiterId == null || u.getId().equals(recruiterId))
                .toList();
        List<RecruiterReportResponse> results = new ArrayList<>();
        for (User u : recruiters) {
            long added = candidateRepository.countByRecruiterBetween(u.getId(), fromInstant, toInstant);
            long contacted = candidateRepository.countByStatusAndDeletedAtIsNull("CONTACTED");
            long screened = candidateRepository.countByStatusAndDeletedAtIsNull("SCREENING");
            long submitted = submissionRepository.countByRecruiterBetween(u.getId(), fromInstant, toInstant);
            long interviews = interviewRepository.countByRecruiterBetween(u.getId(), fromInstant, toInstant);
            long placements = candidateRepository.countByStatusAndDeletedAtIsNull("PLACED");
            results.add(RecruiterReportResponse.builder()
                    .recruiterId(u.getId())
                    .recruiterName(u.getFullName())
                    .candidatesAdded(added)
                    .candidatesContacted(contacted)
                    .candidatesScreened(screened)
                    .candidatesSubmitted(submitted)
                    .interviews(interviews)
                    .placements(placements)
                    .build());
        }
        return results;
    }

    @Transactional(readOnly = true)
    public List<JobReportItem> jobReport(Long clientId, String status) {
        List<Job> jobs = jobRepository.findFiltered(
                status == null || status.isBlank() ? null : status,
                clientId, null, null, null);
        List<JobReportItem> items = new ArrayList<>();
        for (Job job : jobs) {
            long pipelineCount = jobCandidateRepository.findByJobIdOrderByUpdatedAtDesc(job.getId()).size();
            long submissions = submissionRepository.findByJobIdOrderBySubmissionDateDesc(job.getId()).size();
            long interviews = interviewRepository.findFiltered(job.getId(), null, null, null, null).size();
            long placements = jobCandidateRepository.countByJobIdAndStatus(job.getId(), "PLACED");
            Long daysOpen = null;
            if (job.getCreatedAt() != null) {
                daysOpen = ChronoUnit.DAYS.between(job.getCreatedAt().atZone(ZoneOffset.UTC).toLocalDate(), LocalDate.now());
            }
            items.add(JobReportItem.builder()
                    .jobId(job.getId())
                    .jobCode(job.getJobCode())
                    .title(job.getTitle())
                    .status(job.getStatus())
                    .candidatesInPipeline(pipelineCount)
                    .submissions(submissions)
                    .interviews(interviews)
                    .placements(placements)
                    .daysOpen(daysOpen)
                    .build());
        }
        return items;
    }

    @Transactional(readOnly = true)
    public List<ClientReportItem> clientReport(Long clientId) {
        List<Client> clients = clientId == null
                ? clientRepository.findFiltered(null, null, null, null)
                : List.of(clientRepository.findByIdAndDeletedAtIsNull(clientId)
                .orElseThrow(() -> new com.staffcore33.ats.common.ResourceNotFoundException("Client not found")));
        List<ClientReportItem> items = new ArrayList<>();
        for (Client client : clients) {
            long jobs = jobRepository.findByClientIdAndDeletedAtIsNull(client.getId()).size();
            long submissions = submissionRepository.findByClientIdOrderBySubmissionDateDesc(client.getId()).size();
            long interviews = interviewRepository.findFiltered(null, null, client.getId(), null, null).size();
            long placements = jobRepository.findByClientIdAndDeletedAtIsNull(client.getId()).stream()
                    .mapToLong(j -> jobCandidateRepository.countByJobIdAndStatus(j.getId(), "PLACED"))
                    .sum();
            items.add(ClientReportItem.builder()
                    .clientId(client.getId())
                    .companyName(client.getCompanyName())
                    .jobsReceived(jobs)
                    .candidatesSubmitted(submissions)
                    .interviews(interviews)
                    .placements(placements)
                    .build());
        }
        return items;
    }

    @Transactional(readOnly = true)
    public ConversionReportResponse conversionReport() {
        Map<String, Long> stages = new LinkedHashMap<>();
        String[] order = {
                "SOURCED", "CONTACTED", "INTERESTED", "SCREENING", "SUBMITTED",
                "CLIENT_REVIEW", "INTERVIEW", "SELECTED", "ONBOARDING", "PLACED"
        };
        Map<String, Long> grouped = new LinkedHashMap<>();
        for (Object[] row : jobCandidateRepository.countGroupedByStatus()) {
            grouped.put(String.valueOf(row[0]), (Long) row[1]);
        }
        for (String stage : order) {
            stages.put(stage, grouped.getOrDefault(stage, 0L));
        }

        Map<String, Double> rates = new LinkedHashMap<>();
        rates.put("contactToInterested", rate(stages.get("CONTACTED"), stages.get("INTERESTED")));
        rates.put("interestedToScreening", rate(stages.get("INTERESTED"), stages.get("SCREENING")));
        rates.put("screeningToSubmission", rate(stages.get("SCREENING"), stages.get("SUBMITTED")));
        rates.put("submissionToInterview", rate(stages.get("SUBMITTED"), stages.get("INTERVIEW")));
        rates.put("interviewToPlacement", rate(stages.get("INTERVIEW"), stages.get("PLACED")));

        return ConversionReportResponse.builder()
                .stageCounts(stages)
                .conversionRates(rates)
                .build();
    }

    private double rate(Long from, Long to) {
        if (from == null || from == 0) {
            return 0.0;
        }
        return Math.round((to == null ? 0.0 : to.doubleValue()) / from.doubleValue() * 10000.0) / 100.0;
    }

    private Instant toInstantStart(LocalDate from) {
        return from == null
                ? Instant.EPOCH
                : from.atStartOfDay().toInstant(ZoneOffset.UTC);
    }

    private Instant toInstantEnd(LocalDate to) {
        return to == null
                ? Instant.parse("9999-12-31T23:59:59Z")
                : to.atTime(LocalTime.MAX).toInstant(ZoneOffset.UTC);
    }
}
