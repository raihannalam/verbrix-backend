package com.verbrix.payload.payment.response;

import com.verbrix.model.enums.PaymentStatus;
import com.verbrix.model.enums.TransactionPurpose;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
public class TransactionResponse {
    private Long id;
    private String razorpayOrderId;
    private String razorpayPaymentId;
    private BigDecimal amount;
    private String currency;
    private TransactionPurpose purpose;
    private PaymentStatus status;
    private Instant createdAt;

    // Context: Who was this transaction with?
    private Long relationshipId;
    private String otherPartyName; // If I am Client, this is Interpreter Name.
}