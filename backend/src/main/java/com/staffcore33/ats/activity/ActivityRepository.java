package com.staffcore33.ats.activity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ActivityRepository extends JpaRepository<Activity, Long> {

    @Query("""
            SELECT a FROM Activity a
            WHERE (:candidateId IS NULL OR a.candidateId = :candidateId)
              AND (:jobId IS NULL OR a.jobId = :jobId)
              AND (:clientId IS NULL OR a.clientId = :clientId)
              AND (:activityType IS NULL OR a.activityType = :activityType)
            ORDER BY a.activityAt DESC
            """)
    List<Activity> findFiltered(
            @Param("candidateId") Long candidateId,
            @Param("jobId") Long jobId,
            @Param("clientId") Long clientId,
            @Param("activityType") String activityType);

    List<Activity> findTop20ByOrderByActivityAtDesc();
}
