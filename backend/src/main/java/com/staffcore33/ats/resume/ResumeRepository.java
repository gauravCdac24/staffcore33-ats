package com.staffcore33.ats.resume;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ResumeRepository extends JpaRepository<Resume, Long> {
    List<Resume> findByCandidateIdOrderByCreatedAtDesc(Long candidateId);
    Optional<Resume> findByIdAndCandidateId(Long id, Long candidateId);
}
