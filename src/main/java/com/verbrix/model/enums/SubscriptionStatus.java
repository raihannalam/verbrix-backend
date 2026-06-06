package com.verbrix.model.enums;

public enum SubscriptionStatus {
    CREATED,        // 1. Link generated, waiting for user auth
    AUTHENTICATED,  // 2. User authorized, waiting for first charge
    ACTIVE,         // 3. Payments flowing successfully
    HALTED,         // 4. Payment failed (Insufficient funds), retrying
    CANCELLED,      // 5. Canceled by user or interpreter
    COMPLETED,       // 6. End of fixed term (if applicable)
    DISPUTED
}