package com.verbrix.controller;

import com.verbrix.payload.payment.OrderResponse;
import com.verbrix.payload.payment.PaymentVerificationRequest; // Ensure you create this class
import com.verbrix.payload.payment.response.TransactionResponse;
import com.verbrix.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    // Create Order
    @PostMapping("/consultation/{relationshipId}")
    public ResponseEntity<OrderResponse> createOrder(@PathVariable Long relationshipId, @AuthenticationPrincipal UserDetails userDetails) {
        // Note: Passed user email logic moved to service or handled via principal
        return ResponseEntity.ok(paymentService.createConsultationOrder(relationshipId, userDetails.getUsername()));
        // Better: Use @AuthenticationPrincipal inside service as before
    }

    // 🟢 NEW: Verify Endpoint
    @PostMapping("/verify")
    public ResponseEntity<?> verifyPayment(@RequestBody PaymentVerificationRequest request) {
        paymentService.verifyPayment(request);
        return ResponseEntity.ok().body("{\"message\": \"Payment verified successfully\"}");
    }

    @GetMapping("/history")
    @PreAuthorize("isAuthenticated()") // Both Clients and Interpreters can see this
    public ResponseEntity<Page<TransactionResponse>> getMyTransactionHistory(
            @AuthenticationPrincipal UserDetails userDetails,
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ResponseEntity.ok(paymentService.getMyTransactions(userDetails.getUsername(), pageable));
    }
}