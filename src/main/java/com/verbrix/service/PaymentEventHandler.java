package com.verbrix.service;

import com.verbrix.model.business.payment.Transaction;
import com.verbrix.model.enums.PaymentStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PaymentEventHandler {

    private final RelationshipService relationshipService;

    public void handle(String eventType,
                       Transaction transaction,
                       String razorpayPaymentId) {

        switch (eventType) {

            case "payment.captured" -> {
                transaction.setPaymentStatus(PaymentStatus.SUCCESS);
                transaction.setRazorpayPaymentId(razorpayPaymentId);

                relationshipService.onPaymentSuccess(transaction.getId());
            }

//            case "payment.failed" -> {
//                transaction.setPaymentStatus(PaymentStatus.FAILED);
//                transaction.setRazorpayPaymentId(razorpayPaymentId);
//
//                relationshipService.onPaymentFailure(transaction);
//            }
//
//            case "refund.processed" -> {
//                transaction.setPaymentStatus(PaymentStatus.REFUNDED);
//
//                relationshipService.onRefund(transaction);
//            }

            default -> log.info("Unhandled Razorpay event: {}", eventType);
        }
    }
}
