package com.verbrix.repository;

import com.verbrix.model.rbac.User;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    /**
     * Deletes users who never completed registration
     * within the allowed grace period.
     *
     * SECURITY:
     * - JPQL (not native SQL)
     * - Named parameters
     * - No string interpolation
     * - Injection-safe by construction
     */
    @Modifying
    @Transactional
    @Query("""
        DELETE FROM User u
        WHERE u.isActive = false
          AND u.createdAt < :cutoff
    """)
    int deleteUnverifiedUsersOlderThan(
            @Param("cutoff") Instant cutoff
    );

    boolean existsByEmail(String email);
}
