package com.staffcore33.ats.pipeline;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface JobCandidateRepository extends JpaRepository<JobCandidate, Long> {

    Optional<JobCandidate> findByJobIdAndCandidateId(Long jobId, Long candidateId);

    List<JobCandidate> findByJobIdOrderByUpdatedAtDesc(Long jobId);

    List<JobCandidate> findByCandidateIdOrderByUpdatedAtDesc(Long candidateId);

    long countByStatus(String status);

    @Query("""
            SELECT jc.status, COUNT(jc) FROM JobCandidate jc
            GROUP BY jc.status
            """)
    List<Object[]> countGroupedByStatus();

    @Query("""
            SELECT COUNT(jc) FROM JobCandidate jc
            WHERE jc.jobId = :jobId AND jc.status = :status
            """)
    long countByJobIdAndStatus(@Param("jobId") Long jobId, @Param("status") String status);
}
