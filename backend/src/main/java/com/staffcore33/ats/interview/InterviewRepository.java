package com.staffcore33.ats.interview;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;

public interface InterviewRepository extends JpaRepository<Interview, Long> {

    @Query("""
            SELECT i FROM Interview i
            WHERE (:jobId IS NULL OR i.jobId = :jobId)
              AND (:candidateId IS NULL OR i.candidateId = :candidateId)
              AND (:clientId IS NULL OR i.clientId = :clientId)
              AND (:status IS NULL OR i.status = :status)
              AND (:recruiterId IS NULL OR i.recruiterId = :recruiterId)
            ORDER BY i.interviewDate DESC, i.id DESC
            """)
    List<Interview> findFiltered(
            @Param("jobId") Long jobId,
            @Param("candidateId") Long candidateId,
            @Param("clientId") Long clientId,
            @Param("status") String status,
            @Param("recruiterId") Long recruiterId);

    long countByStatus(String status);

    @Query("""
            SELECT COUNT(i) FROM Interview i
            WHERE i.createdAt >= :from
              AND i.createdAt <= :to
            """)
    long countBetween(@Param("from") Instant from, @Param("to") Instant to);

    @Query("""
            SELECT COUNT(i) FROM Interview i
            WHERE i.recruiterId = :recruiterId
              AND i.createdAt >= :from
              AND i.createdAt <= :to
            """)
    long countByRecruiterBetween(
            @Param("recruiterId") Long recruiterId,
            @Param("from") Instant from,
            @Param("to") Instant to);

    @Query("""
            SELECT COUNT(i) FROM Interview i
            WHERE i.status = 'SELECTED'
              AND i.createdAt >= :from
              AND i.createdAt <= :to
            """)
    long countPlacementsBetween(@Param("from") Instant from, @Param("to") Instant to);
}
