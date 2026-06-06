package com.verbrix.model.business.payment;

import com.verbrix.model.business.booking.Relationship;
import com.verbrix.model.enums.RecurringFeeFrequency;
import com.verbrix.model.enums.SubscriptionStatus;
import com.verbrix.model.rbac.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "subscriptions",
       uniqueConstraints = {
           @UniqueConstraint(columnNames = {"razorpay_subscription_id"})
       })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Subscription { // Renamed from PaymentMandate

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "relationship_id", nullable = false)
    private Relationship relationship;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "client_id", nullable = false)
    private User client;

    // --- SNAPSHOT OF THE DEAL ---
    // Critical: Even if Interpreter changes their profile fee later, 
    // this subscription continues at the agreed price.
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RecurringFeeFrequency frequency;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    // --- PROVIDER DETAILS ---
    @Column(name = "razorpay_subscription_id", unique = true)
    private String razorpaySubscriptionId;

    @Column(name = "razorpay_plan_id")
    private String razorpayPlanId;

    // --- STATE MANAGEMENT ---
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SubscriptionStatus status; 
    // Values: CREATED, AUTHENTICATED, ACTIVE, HALTED, CANCELLED, COMPLETED

    // --- COVERAGE TRACKING (The "Meter") ---
    
    /** When the current paid period ends. Access is denied after this time. */
    private Instant currentPeriodEnd;

    /** When the cycle actually started */
    private Instant currentPeriodStart;

    /** Total count of successful charges */
    @Column(columnDefinition = "integer default 0")
    private int successfulChargeCount;

    // --- AUDIT ---
    @CreationTimestamp
    private Instant createdAt;

    @UpdateTimestamp
    private Instant updatedAt;
    
    // Helper to check access
    public boolean isAccessActive() {
        if (this.status != SubscriptionStatus.ACTIVE) return false;
        // Allow a small grace period (e.g. 24 hours) for payment processing
        return currentPeriodEnd != null && currentPeriodEnd.isAfter(Instant.now().minusSeconds(86400));
    }
}