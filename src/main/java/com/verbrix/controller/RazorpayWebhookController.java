package com.verbrix.controller;

import com.verbrix.model.business.payment.Transaction;
import com.verbrix.model.enums.PaymentStatus;
import com.verbrix.repository.TransactionRepository;
import com.verbrix.service.PaymentEventHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.HexFormat;

@RestController
@RequestMapping("/api/v1/webhooks")
@RequiredArgsConstructor
@Slf4j
public class RazorpayWebhookController {

    private final TransactionRepository transactionRepository;
    private final PaymentEventHandler paymentEventHandler;

    @Value("${razorpay.webhook.secret}")
    private String webhookSecret;

    @PostMapping("/razorpay")
    public ResponseEntity<String> handleRazorpayWebhook(
            @RequestBody(required = false) String payload,
            @RequestHeader(value = "X-Razorpay-Signature", required = false) String signature
    ) {

        if (payload == null || payload.isBlank()) {
            return ResponseEntity.badRequest().body("Empty payload");
        }

        if (signature == null || signature.isBlank()) {
            return ResponseEntity.badRequest().body("Missing signature");
        }

        if (!isSignatureValid(payload, signature)) {
            return ResponseEntity.badRequest().body("Invalid signature");
        }

        JSONObject event = new JSONObject(payload);
        String eventType = event.optString("event");

        JSONObject entity = event
                .optJSONObject("payload")
                .optJSONObject("payment")
                .optJSONObject("entity");

        if (entity == null) {
            return ResponseEntity.ok("Ignored");
        }

        String orderId = entity.optString("order_id");
        String paymentId = entity.optString("id");

        Transaction transaction = transactionRepository
                .findByRazorpayOrderId(orderId)
                .orElse(null);

        if (transaction == null) {
            log.warn("Transaction not found for orderId={}", orderId);
            return ResponseEntity.ok("Ignored");
        }

        // Idempotency
        if (transaction.getPaymentStatus() == PaymentStatus.SUCCESS ||
                transaction.getPaymentStatus() == PaymentStatus.FAILED) {
            return ResponseEntity.ok("Already processed");
        }

        // Delegate business meaning
        paymentEventHandler.handle(eventType, transaction, paymentId);

        transactionRepository.save(transaction);
        return ResponseEntity.ok("Processed");
    }

    private boolean isSignatureValid(String payload, String actualSignature) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(
                    webhookSecret.getBytes(StandardCharsets.UTF_8),
                    "HmacSHA256"
            ));
            String expected = HexFormat.of()
                    .formatHex(mac.doFinal(payload.getBytes(StandardCharsets.UTF_8)));
            return expected.equals(actualSignature);
        } catch (Exception e) {
            return false;
        }
    }
}
