package com.staffcore33.ats.importexport;

import com.staffcore33.ats.candidate.Candidate;
import com.staffcore33.ats.candidate.CandidateRepository;
import com.staffcore33.ats.candidate.CandidateRequest;
import com.staffcore33.ats.candidate.CandidateService;
import com.staffcore33.ats.common.BadRequestException;
import com.staffcore33.ats.job.Job;
import com.staffcore33.ats.job.JobRepository;
import com.staffcore33.ats.submission.Submission;
import com.staffcore33.ats.submission.SubmissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ImportExportService {

    private final CandidateService candidateService;
    private final CandidateRepository candidateRepository;
    private final JobRepository jobRepository;
    private final SubmissionRepository submissionRepository;

    @Transactional
    public Map<String, Object> importCandidates(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("CSV file is required");
        }
        int imported = 0;
        int skipped = 0;
        List<String> errors = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            String headerLine = reader.readLine();
            if (headerLine == null) {
                throw new BadRequestException("CSV is empty");
            }
            String[] headers = splitCsv(headerLine);
            Map<String, Integer> index = new HashMap<>();
            for (int i = 0; i < headers.length; i++) {
                index.put(normalizeHeader(headers[i]), i);
            }
            String line;
            int row = 1;
            while ((line = reader.readLine()) != null) {
                row++;
                if (line.isBlank()) {
                    continue;
                }
                try {
                    String[] cols = splitCsv(line);
                    CandidateRequest request = new CandidateRequest();
                    request.setFirstName(required(cols, index, "first_name", "firstname", "first name"));
                    request.setLastName(required(cols, index, "last_name", "lastname", "last name"));
                    request.setEmail(optional(cols, index, "email"));
                    request.setPhone(optional(cols, index, "phone"));
                    request.setCity(optional(cols, index, "city", "location"));
                    request.setState(optional(cols, index, "state"));
                    request.setLinkedinUrl(optional(cols, index, "linkedin", "linkedin_url"));
                    request.setPrimarySkills(optional(cols, index, "skills", "primary_skills"));
                    String exp = optional(cols, index, "experience", "total_experience_years");
                    if (exp != null && !exp.isBlank()) {
                        request.setTotalExperienceYears(new BigDecimal(exp.trim()));
                    }
                    request.setWorkAuthorization(optional(cols, index, "work_authorization", "work authorization"));
                    String rate = optional(cols, index, "rate", "desired_rate");
                    if (rate != null && !rate.isBlank()) {
                        request.setDesiredRate(new BigDecimal(rate.trim()));
                    }
                    candidateService.create(request);
                    imported++;
                } catch (Exception e) {
                    skipped++;
                    errors.add("Row " + row + ": " + e.getMessage());
                }
            }
        } catch (BadRequestException e) {
            throw e;
        } catch (Exception e) {
            throw new BadRequestException("Failed to read CSV: " + e.getMessage());
        }
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("imported", imported);
        result.put("skipped", skipped);
        result.put("errors", errors);
        return result;
    }

    @Transactional(readOnly = true)
    public String exportCandidatesCsv() {
        StringBuilder sb = new StringBuilder();
        sb.append("candidate_code,first_name,last_name,email,phone,city,state,primary_skills,total_experience_years,work_authorization,desired_rate,status\n");
        for (Candidate c : candidateRepository.findByDeletedAtIsNull()) {
            sb.append(csv(c.getCandidateCode())).append(',')
                    .append(csv(c.getFirstName())).append(',')
                    .append(csv(c.getLastName())).append(',')
                    .append(csv(c.getEmail())).append(',')
                    .append(csv(c.getPhone())).append(',')
                    .append(csv(c.getCity())).append(',')
                    .append(csv(c.getState())).append(',')
                    .append(csv(c.getPrimarySkills())).append(',')
                    .append(csv(c.getTotalExperienceYears())).append(',')
                    .append(csv(c.getWorkAuthorization())).append(',')
                    .append(csv(c.getDesiredRate())).append(',')
                    .append(csv(c.getStatus())).append('\n');
        }
        return sb.toString();
    }

    @Transactional(readOnly = true)
    public String exportJobsCsv() {
        StringBuilder sb = new StringBuilder();
        sb.append("job_code,title,client_id,location,status,priority,pay_rate,bill_rate,assigned_recruiter_id\n");
        for (Job j : jobRepository.findFiltered(null, null, null, null, null)) {
            sb.append(csv(j.getJobCode())).append(',')
                    .append(csv(j.getTitle())).append(',')
                    .append(csv(j.getClientId())).append(',')
                    .append(csv(j.getLocation())).append(',')
                    .append(csv(j.getStatus())).append(',')
                    .append(csv(j.getPriority())).append(',')
                    .append(csv(j.getPayRate())).append(',')
                    .append(csv(j.getBillRate())).append(',')
                    .append(csv(j.getAssignedRecruiterId())).append('\n');
        }
        return sb.toString();
    }

    @Transactional(readOnly = true)
    public String exportSubmissionsCsv() {
        StringBuilder sb = new StringBuilder();
        sb.append("id,candidate_id,job_id,client_id,submission_date,submitted_rate,bill_rate,status,submitted_by\n");
        for (Submission s : submissionRepository.findFiltered(null, null, null, null, null)) {
            sb.append(csv(s.getId())).append(',')
                    .append(csv(s.getCandidateId())).append(',')
                    .append(csv(s.getJobId())).append(',')
                    .append(csv(s.getClientId())).append(',')
                    .append(csv(s.getSubmissionDate())).append(',')
                    .append(csv(s.getSubmittedRate())).append(',')
                    .append(csv(s.getBillRate())).append(',')
                    .append(csv(s.getStatus())).append(',')
                    .append(csv(s.getSubmittedBy())).append('\n');
        }
        return sb.toString();
    }

    private String required(String[] cols, Map<String, Integer> index, String... keys) {
        String value = optional(cols, index, keys);
        if (value == null || value.isBlank()) {
            throw new BadRequestException("Missing required field: " + keys[0]);
        }
        return value.trim();
    }

    private String optional(String[] cols, Map<String, Integer> index, String... keys) {
        for (String key : keys) {
            Integer i = index.get(normalizeHeader(key));
            if (i != null && i < cols.length) {
                return cols[i].trim();
            }
        }
        return null;
    }

    private String normalizeHeader(String h) {
        return h == null ? "" : h.trim().toLowerCase(Locale.ROOT).replace('-', '_').replace(' ', '_');
    }

    private String[] splitCsv(String line) {
        List<String> result = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                result.add(current.toString());
                current.setLength(0);
            } else {
                current.append(c);
            }
        }
        result.add(current.toString());
        return result.toArray(new String[0]);
    }

    private String csv(Object value) {
        if (value == null) {
            return "";
        }
        String s = String.valueOf(value);
        if (s.contains(",") || s.contains("\"") || s.contains("\n")) {
            return "\"" + s.replace("\"", "\"\"") + "\"";
        }
        return s;
    }
}
