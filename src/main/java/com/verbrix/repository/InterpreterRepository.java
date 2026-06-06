package com.verbrix.repository;

import com.verbrix.model.enums.VerificationStatus;
import com.verbrix.model.profile.Interpreter;
import com.verbrix.model.rbac.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface InterpreterRepository extends JpaRepository<Interpreter, Long> {
    boolean existsByUser(User user);

    Optional<Interpreter> findByUser(User user);

    Page<Interpreter> findAll(Pageable pageable);

    Page<Interpreter> findAllByStatusAndAvailableTrue(VerificationStatus status, Pageable pageable);


    @Query("SELECT i FROM Interpreter i JOIN FETCH i.user WHERE i.status = :status")
    List<Interpreter> findAllByStatus(@Param("status") VerificationStatus status);

    @Query("SELECT i FROM Interpreter i JOIN FETCH i.user")
    List<Interpreter> findAllWithUser();

    @Query("SELECT i FROM Interpreter i " +
            "JOIN FETCH i.user " +
            "LEFT JOIN FETCH i.specializations " +
            "WHERE i.id = :id")
    Optional<Interpreter> findByIdWithDetails(@Param("id") Long id);

    List<Interpreter> findByStatus(VerificationStatus status);
}