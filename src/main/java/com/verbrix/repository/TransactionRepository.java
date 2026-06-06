package com.verbrix.repository;

import com.verbrix.model.business.booking.Relationship;
import com.verbrix.model.business.payment.Transaction;
import com.verbrix.model.enums.PaymentStatus;
import com.verbrix.model.enums.TransactionPurpose;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    Optional<Transaction> findByRazorpayOrderId(String orderId);

    @Query("""
        select t
        from Transaction t
        join fetch t.relationship r
        join fetch r.client
        join fetch r.interpreter
        where t.id = :id
    """)
    Optional<Transaction> findByIdWithRelationship(@Param("id") Long id);

    // 🟢 FIX 1: Return List instead of Optional to handle the "9 results" crash
    // We order by CreatedAt DESC so the first item (index 0) is the most recent attempt
    List<Transaction> findByRelationshipAndPurposeAndPaymentStatusOrderByCreatedAtDesc(
            Relationship relationship,
            TransactionPurpose purpose,
            PaymentStatus paymentStatus
    );

    // 🟢 FIX 2: Add this to efficiently check if they already paid successfully
    boolean existsByRelationshipAndPurposeAndPaymentStatus(
            Relationship relationship,
            TransactionPurpose purpose,
            PaymentStatus paymentStatus
    );

    @Query("SELECT t FROM Transaction t " +
            "JOIN t.relationship r " +
            "WHERE r.client.id = :userId OR r.interpreter.id = :userId " +
            "ORDER BY t.createdAt DESC")
    Page<Transaction> findAllByUserId(@Param("userId") Long userId, Pageable pageable);
}