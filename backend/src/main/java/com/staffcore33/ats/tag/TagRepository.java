package com.staffcore33.ats.tag;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TagRepository extends JpaRepository<Tag, Long> {

    Optional<Tag> findByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCase(String name);

    @Query(value = """
            SELECT t.name FROM tags t
            INNER JOIN candidate_tags ct ON ct.tag_id = t.id
            WHERE ct.candidate_id = :candidateId
            ORDER BY t.name
            """, nativeQuery = true)
    List<String> findNamesByCandidateId(@Param("candidateId") Long candidateId);

    @Query(value = """
            SELECT t.* FROM tags t
            INNER JOIN candidate_tags ct ON ct.tag_id = t.id
            WHERE ct.candidate_id = :candidateId
            ORDER BY t.name
            """, nativeQuery = true)
    List<Tag> findByCandidateId(@Param("candidateId") Long candidateId);

    @Modifying(clearAutomatically = true)
    @Query(value = "DELETE FROM candidate_tags WHERE candidate_id = :candidateId AND tag_id = :tagId", nativeQuery = true)
    void removeCandidateTag(@Param("candidateId") Long candidateId, @Param("tagId") Long tagId);

    @Modifying(clearAutomatically = true)
    @Query(value = """
            INSERT INTO candidate_tags (candidate_id, tag_id)
            SELECT :candidateId, :tagId
            WHERE NOT EXISTS (
                SELECT 1 FROM candidate_tags WHERE candidate_id = :candidateId AND tag_id = :tagId
            )
            """, nativeQuery = true)
    void assignCandidateTag(@Param("candidateId") Long candidateId, @Param("tagId") Long tagId);

    @Query("SELECT t FROM Tag t WHERE LOWER(t.name) LIKE CONCAT('%', LOWER(CAST(:search AS string)), '%') ORDER BY t.name")
    List<Tag> search(@Param("search") String search);
}
