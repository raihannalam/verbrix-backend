package com.verbrix.model.enums;

public enum PaymentStatus {
    CREATED,      // 1. Order ID generated in Razorpay. User is on the checkout screen.
    PROCESSING,   // 2. User completed payment (Authorized), waiting for Webhook/Capture.
    SUCCESS,      // 3. Money securely captured. Feature is unlocked.
    FAILED,       // 4. Transaction failed or declined.
    REFUNDED,      // 5. Money returned to user.
    REFUND_FAILED
}