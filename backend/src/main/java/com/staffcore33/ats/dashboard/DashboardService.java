package com.staffcore33.ats.dashboard;

import com.staffcore33.ats.activity.ActivityRepository;
import com.staffcore33.ats.candidate.CandidateRepository;
import com.staffcore33.ats.job.JobRepository;
import com.staffcore33.ats.pipeline.JobCandidateRepository;
import com.staffcore33.ats.submission.SubmissionRepository;
import com.staffcore33.ats.task.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final JobRepository jobRepository;
    private final CandidateRepository candidateRepository;
    private final JobCandidateRepository jobCandidateRepository;
    private final TaskRepository taskRepository;
    private final SubmissionRepository submissionRepository;
    private final ActivityRepository activityRepository;

    @Transactional(readOnly = true)
    public DashboardSummaryResponse summary(LocalDate from, LocalDate to) {
        Instant fromInstant = from == null
                ? Instant.EPOCH
                : from.atStartOfDay().toInstant(ZoneOffset.UTC);
        Instant toInstant = to == null
                ? Instant.parse("9999-12-31T23:59:59Z")
                : to.atTime(LocalTime.MAX).toInstant(ZoneOffset.UTC);

        Map<String, Long> jobs = new LinkedHashMap<>();
        jobs.put("total", jobRepository.countByDeletedAtIsNull());
        jobs.put("new", jobRepository.countByStatusAndDeletedAtIsNull("NEW"));
        jobs.put("open", jobRepository.countByStatusAndDeletedAtIsNull("OPEN"));
        jobs.put("inProgress", jobRepository.countByStatusAndDeletedAtIsNull("IN_PROGRESS"));
        jobs.put("onHold", jobRepository.countByStatusAndDeletedAtIsNull("ON_HOLD"));
        jobs.put("filled", jobRepository.countByStatusAndDeletedAtIsNull("FILLED"));
        jobs.put("closed", jobRepository.countByStatusAndDeletedAtIsNull("CLOSED"));
        jobs.put("createdInRange", jobRepository.countCreatedBetween(fromInstant, toInstant));

        Map<String, Long> candidates = new LinkedHashMap<>();
        candidates.put("total", candidateRepository.countByDeletedAtIsNull());
        candidates.put("new", candidateRepository.countByStatusAndDeletedAtIsNull("NEW"));
        candidates.put("contacted", candidateRepository.countByStatusAndDeletedAtIsNull("CONTACTED"));
        candidates.put("screening", candidateRepository.countByStatusAndDeletedAtIsNull("SCREENING"));
        candidates.put("submitted", candidateRepository.countByStatusAndDeletedAtIsNull("SUBMITTED"));
        candidates.put("interviewing", candidateRepository.countByStatusAndDeletedAtIsNull("INTERVIEWING"));
        candidates.put("placed", candidateRepository.countByStatusAndDeletedAtIsNull("PLACED"));
        candidates.put("createdInRange", candidateRepository.countCreatedBetween(fromInstant, toInstant));

        Map<String, Long> pipeline = new LinkedHashMap<>();
        for (Object[] row : jobCandidateRepository.countGroupedByStatus()) {
            pipeline.put(String.valueOf(row[0]), (Long) row[1]);
        }

        LocalDate today = LocalDate.now();
        Map<String, Long> tasks = new LinkedHashMap<>();
        tasks.put("today", taskRepository.countDueToday(today));
        tasks.put("overdue", taskRepository.countOverdue(today));

        return DashboardSummaryResponse.builder()
                .jobs(jobs)
                .candidates(candidates)
                .pipeline(pipeline)
                .tasks(tasks)
                .recentSubmissions(submissionRepository.countBetween(fromInstant, toInstant))
                .totalActivities(activityRepository.count())
                .build();
    }
}
