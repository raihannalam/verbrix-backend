package com.verbrix.model.enums;

public enum RelationshipStatus {


    REQUESTED,                 // Client sent request
    REQUEST_ACCEPTED,          // Interpreter accepted, chat enabled


    CONSULTATION_PENDING_PAYMENT, // Waiting for consultation fee
    CONSULTATION_ACTIVE,          // Consultation in progress
    CONSULTATION_COMPLETED,       // Consultation finished



    AGREEMENT_PENDING_PAYMENT, // Waiting for service agreement fee
    AGREEMENT_ACTIVE,          // 🔒 Legally & professionally bound



    WORK_ACTIVE,               // Ongoing work (recurring invoices apply)
    WORK_PAUSED,               // Paused due to missed invoice / admin action


    DISPUTED,                  // Dispute raised by either party
    SUSPENDED,                 // Admin-enforced suspension



    COMPLETED,                 // Work completed successfully
    TERMINATED                 // Relationship ended prematurely
}
