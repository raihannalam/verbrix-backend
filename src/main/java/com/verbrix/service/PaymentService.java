package com.verbrix.service;

import com.verbrix.payload.payment.OrderResponse;
import com.verbrix.payload.payment.PaymentVerificationRequest;
import com.verbrix.payload.payment.response.TransactionResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PaymentService {
    public OrderResponse createConsultationOrder(Long relationshipId, String userEmail);

    // 🟢 NEW METHOD: VERIFY PAYMENT & UNLOCK
    void verifyPayment(PaymentVerificationRequest request);

    void refundTransaction(Long transactionId);

    Page<TransactionResponse> getMyTransactions(String userEmail, Pageable pageable);
}
