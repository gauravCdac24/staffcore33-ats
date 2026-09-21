package com.staffcore33.ats.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmailIgnoreCaseAndDeletedAtIsNull(String email);
    Optional<User> findByIdAndDeletedAtIsNull(Long id);
    boolean existsByEmailIgnoreCaseAndDeletedAtIsNull(String email);

    @Query("SELECT u FROM User u WHERE u.deletedAt IS NULL ORDER BY u.fullName")
    List<User> findAllActive();

    @Query("SELECT u FROM User u WHERE u.deletedAt IS NOT NULL ORDER BY u.updatedAt DESC")
    List<User> findAllDeleted();
}
