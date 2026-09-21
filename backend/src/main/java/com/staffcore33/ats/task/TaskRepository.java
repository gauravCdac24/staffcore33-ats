package com.staffcore33.ats.task;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Long> {

    @Query("""
            SELECT t FROM Task t
            WHERE (:assignedTo IS NULL OR t.assignedTo = :assignedTo)
              AND (:status IS NULL OR t.status = :status)
              AND (:candidateId IS NULL OR t.candidateId = :candidateId)
              AND (:jobId IS NULL OR t.jobId = :jobId)
              AND (:clientId IS NULL OR t.clientId = :clientId)
              AND (:filter IS NULL
                   OR (:filter = 'today' AND t.dueDate = :today)
                   OR (:filter = 'overdue' AND t.dueDate < :today AND t.status <> 'DONE' AND t.status <> 'COMPLETED')
                   OR (:filter = 'upcoming' AND t.dueDate > :today AND t.status <> 'DONE' AND t.status <> 'COMPLETED'))
            ORDER BY t.dueDate ASC, t.priority DESC
            """)
    List<Task> findFiltered(
            @Param("assignedTo") Long assignedTo,
            @Param("status") String status,
            @Param("candidateId") Long candidateId,
            @Param("jobId") Long jobId,
            @Param("clientId") Long clientId,
            @Param("filter") String filter,
            @Param("today") LocalDate today);

    long countByDueDateAndStatusNotIn(LocalDate dueDate, List<String> statuses);

    @Query("""
            SELECT COUNT(t) FROM Task t
            WHERE t.dueDate < :today
              AND t.status NOT IN ('DONE', 'COMPLETED', 'CANCELLED')
            """)
    long countOverdue(@Param("today") LocalDate today);

    @Query("""
            SELECT COUNT(t) FROM Task t
            WHERE t.dueDate = :today
              AND t.status NOT IN ('DONE', 'COMPLETED', 'CANCELLED')
            """)
    long countDueToday(@Param("today") LocalDate today);
}
