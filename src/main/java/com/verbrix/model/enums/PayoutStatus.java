package com.verbrix.model.enums;

public enum PayoutStatus {
    /**
     * The payout has been created but not yet sent to the payment processor.
     */
    PENDING,
    
    /**
     * The payout is currently being processed by the financial institution.
     */
    PROCESSING,
    
    /**
     * The funds have been successfully transferred to the interpreter's account.
     */
    COMPLETED,
    
    /**
     * The payout failed and funds were not transferred.
     */
    FAILED,
    
    /**
     * The payout was cancelled by an admin before processing.
     */
    CANCELLED
}