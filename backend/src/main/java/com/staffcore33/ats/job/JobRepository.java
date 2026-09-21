package com.staffcore33.ats.job;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface JobRepository extends JpaRepository<Job, Long> {

    Optional<Job> findByIdAndDeletedAtIsNull(Long id);

    @Query("""
            SELECT j FROM Job j
            WHERE j.deletedAt IS NULL
              AND (:status IS NULL OR j.status = :status)
              AND (:clientId IS NULL OR j.clientId = :clientId)
              AND (:recruiterId IS NULL OR j.assignedRecruiterId = :recruiterId)
              AND (:priority IS NULL OR j.priority = :priority)
              AND (:search IS NULL OR LOWER(j.title) LIKE CONCAT('%', LOWER(CAST(:search AS string)), '%')
                   OR LOWER(j.jobCode) LIKE CONCAT('%', LOWER(CAST(:search AS string)), '%'))
            ORDER BY j.createdAt DESC
            """)
    List<Job> findFiltered(
            @Param("status") String status,
            @Param("clientId") Long clientId,
            @Param("recruiterId") Long recruiterId,
            @Param("priority") String priority,
            @Param("search") String search);

    @Query("SELECT j FROM Job j WHERE j.deletedAt IS NOT NULL ORDER BY j.updatedAt DESC")
    List<Job> findAllDeleted();

    long countByDeletedAtIsNull();

    long countByStatusAndDeletedAtIsNull(String status);

    @Query("""
            SELECT COUNT(j) FROM Job j
            WHERE j.deletedAt IS NULL
              AND j.createdAt >= :from
              AND j.createdAt <= :to
            """)
    long countCreatedBetween(@Param("from") Instant from, @Param("to") Instant to);

    List<Job> findByClientIdAndDeletedAtIsNull(Long clientId);
}
