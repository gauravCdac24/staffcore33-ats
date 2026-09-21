package com.staffcore33.ats.candidate;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface CandidateRepository extends JpaRepository<Candidate, Long> {

    Optional<Candidate> findByIdAndDeletedAtIsNull(Long id);

    @Query("""
            SELECT c FROM Candidate c
            WHERE c.deletedAt IS NULL
              AND (:status IS NULL OR c.status = :status)
              AND (:recruiterId IS NULL OR c.recruiterId = :recruiterId)
              AND (:city IS NULL OR LOWER(c.city) LIKE CONCAT('%', LOWER(CAST(:city AS string)), '%'))
              AND (:state IS NULL OR LOWER(c.state) = LOWER(CAST(:state AS string)))
              AND (:skills IS NULL OR LOWER(CONCAT(COALESCE(c.primarySkills,''), ' ', COALESCE(c.secondarySkills,'')))
                   LIKE CONCAT('%', LOWER(CAST(:skills AS string)), '%'))
              AND (:search IS NULL OR LOWER(c.firstName) LIKE CONCAT('%', LOWER(CAST(:search AS string)), '%')
                   OR LOWER(c.lastName) LIKE CONCAT('%', LOWER(CAST(:search AS string)), '%')
                   OR LOWER(c.email) LIKE CONCAT('%', LOWER(CAST(:search AS string)), '%')
                   OR LOWER(c.candidateCode) LIKE CONCAT('%', LOWER(CAST(:search AS string)), '%'))
            ORDER BY c.createdAt DESC
            """)
    List<Candidate> findFiltered(
            @Param("status") String status,
            @Param("recruiterId") Long recruiterId,
            @Param("city") String city,
            @Param("state") String state,
            @Param("skills") String skills,
            @Param("search") String search);

    @Query("SELECT c FROM Candidate c WHERE c.deletedAt IS NOT NULL ORDER BY c.updatedAt DESC")
    List<Candidate> findAllDeleted();

    @Query("""
            SELECT c FROM Candidate c
            WHERE c.deletedAt IS NULL
              AND (
                   (:email IS NOT NULL AND LOWER(c.email) = LOWER(CAST(:email AS string)))
                OR (:phone IS NOT NULL AND c.phone = :phone)
                OR (:linkedinUrl IS NOT NULL AND LOWER(c.linkedinUrl) = LOWER(CAST(:linkedinUrl AS string)))
                OR (:firstName IS NOT NULL AND :lastName IS NOT NULL AND :city IS NOT NULL
                    AND LOWER(c.firstName) = LOWER(CAST(:firstName AS string))
                    AND LOWER(c.lastName) = LOWER(CAST(:lastName AS string))
                    AND LOWER(c.city) = LOWER(CAST(:city AS string)))
              )
            ORDER BY c.id
            """)
    List<Candidate> findPotentialDuplicates(
            @Param("email") String email,
            @Param("phone") String phone,
            @Param("linkedinUrl") String linkedinUrl,
            @Param("firstName") String firstName,
            @Param("lastName") String lastName,
            @Param("city") String city);

    long countByDeletedAtIsNull();

    long countByStatusAndDeletedAtIsNull(String status);

    @Query("""
            SELECT COUNT(c) FROM Candidate c
            WHERE c.deletedAt IS NULL
              AND c.createdAt >= :from
              AND c.createdAt <= :to
            """)
    long countCreatedBetween(@Param("from") Instant from, @Param("to") Instant to);

    @Query("""
            SELECT COUNT(c) FROM Candidate c
            WHERE c.deletedAt IS NULL
              AND c.recruiterId = :recruiterId
              AND c.createdAt >= :from
              AND c.createdAt <= :to
            """)
    long countByRecruiterBetween(
            @Param("recruiterId") Long recruiterId,
            @Param("from") Instant from,
            @Param("to") Instant to);

    List<Candidate> findByDeletedAtIsNull();
}
