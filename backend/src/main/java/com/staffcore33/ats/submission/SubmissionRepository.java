package com.staffcore33.ats.submission;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface SubmissionRepository extends JpaRepository<Submission, Long> {

    Optional<Submission> findFirstByCandidateIdAndJobIdOrderBySubmissionDateDesc(Long candidateId, Long jobId);

    boolean existsByCandidateIdAndJobId(Long candidateId, Long jobId);

    List<Submission> findByJobIdOrderBySubmissionDateDesc(Long jobId);

    List<Submission> findByCandidateIdOrderBySubmissionDateDesc(Long candidateId);

    List<Submission> findByClientIdOrderBySubmissionDateDesc(Long clientId);

    @Query("""
            SELECT s FROM Submission s
            WHERE (:jobId IS NULL OR s.jobId = :jobId)
              AND (:candidateId IS NULL OR s.candidateId = :candidateId)
              AND (:clientId IS NULL OR s.clientId = :clientId)
              AND (:status IS NULL OR s.status = :status)
              AND (:submittedBy IS NULL OR s.submittedBy = :submittedBy)
            ORDER BY s.submissionDate DESC, s.id DESC
            """)
    List<Submission> findFiltered(
            @Param("jobId") Long jobId,
            @Param("candidateId") Long candidateId,
            @Param("clientId") Long clientId,
            @Param("status") String status,
            @Param("submittedBy") Long submittedBy);

    long countByStatus(String status);

    @Query("""
            SELECT COUNT(s) FROM Submission s
            WHERE s.createdAt >= :from
              AND s.createdAt <= :to
            """)
    long countBetween(@Param("from") Instant from, @Param("to") Instant to);

    @Query("""
            SELECT COUNT(s) FROM Submission s
            WHERE s.submittedBy = :recruiterId
              AND s.createdAt >= :from
              AND s.createdAt <= :to
            """)
    long countByRecruiterBetween(
            @Param("recruiterId") Long recruiterId,
            @Param("from") Instant from,
            @Param("to") Instant to);
}
