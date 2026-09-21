package com.staffcore33.ats.client;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ClientRepository extends JpaRepository<Client, Long> {

    Optional<Client> findByIdAndDeletedAtIsNull(Long id);

    @Query("""
            SELECT c FROM Client c
            WHERE c.deletedAt IS NULL
              AND (:status IS NULL OR c.status = :status)
              AND (:industry IS NULL OR LOWER(c.industry) LIKE CONCAT('%', LOWER(CAST(:industry AS string)), '%'))
              AND (:accountManagerId IS NULL OR c.accountManagerId = :accountManagerId)
              AND (:search IS NULL OR LOWER(c.companyName) LIKE CONCAT('%', LOWER(CAST(:search AS string)), '%')
                   OR LOWER(c.contactEmail) LIKE CONCAT('%', LOWER(CAST(:search AS string)), '%'))
            ORDER BY c.companyName
            """)
    List<Client> findFiltered(
            @Param("status") String status,
            @Param("industry") String industry,
            @Param("accountManagerId") Long accountManagerId,
            @Param("search") String search);

    @Query("SELECT c FROM Client c WHERE c.deletedAt IS NOT NULL ORDER BY c.updatedAt DESC")
    List<Client> findAllDeleted();

    long countByDeletedAtIsNull();

    long countByStatusAndDeletedAtIsNull(String status);
}
