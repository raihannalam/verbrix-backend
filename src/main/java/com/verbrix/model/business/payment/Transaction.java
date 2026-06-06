package com.verbrix.model.business.payment;

import com.verbrix.model.business.booking.Relationship;
import com.verbrix.model.enums.PaymentStatus;
import com.verbrix.model.enums.TransactionPurpose;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "transactions")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "relationship_id", nullable = false)
    private Relationship relationship;

    @Column(nullable = false, updatable = false)
    private String razorpayOrderId;


    private String razorpayPaymentId;

    @Column(name = "razorpay_subscription_id")
    private String razorpaySubscriptionId;

    @Column(nullable = false, updatable = false)
    private BigDecimal amount;

    @Column(nullable = false, updatable = false)
    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, updatable = false)
    private TransactionPurpose purpose;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus paymentStatus;

    @CreationTimestamp
    @Column(updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    private Instant updatedAt;

}
