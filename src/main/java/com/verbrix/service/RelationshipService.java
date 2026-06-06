package com.verbrix.service;

import com.verbrix.model.business.payment.Transaction;
import com.verbrix.payload.relationship.RelationshipActionRequest;
import com.verbrix.payload.relationship.RelationshipResponse;

import java.util.List;

public interface RelationshipService {

    RelationshipResponse connect(String clientEmail, Long interpreterProfileId, String initialMessage);

    List<RelationshipResponse> getIncomingRequests(String interpreterEmail);

    RelationshipResponse respondToConnectionRequest(Long relationshipId, String interpreterEmail, RelationshipActionRequest request);

    List<RelationshipResponse> getMyActiveRelationships(String email);


    void onPaymentSuccess(Long transactionId);


//
//    void onConsultationPaymentFailed(Long relationshipId);
//
//    /**
//     * Called when service agreement payment fails or is refunded.
//     */
//    void onServiceAgreementPaymentFailed(Long relationshipId);
//
//
//    /* =====================
//       WORKFLOW ACTIONS
//       ===================== */
//
//    /**
//     * Mark consultation as completed (no payment involved).
//     */
//    void markConsultationCompleted(Long relationshipId, String interpreterEmail);
//
//    /**
//     * Pause work due to unpaid recurring invoice or admin action.
//     */
//    void pauseWork(Long relationshipId, String reason);
//
//    /**
//     * Resume work after dues are cleared.
//     */
//    void resumeWork(Long relationshipId);
//
//
//    /* =====================
//       TERMINAL ACTIONS
//       ===================== */
//
//    void raiseDispute(Long relationshipId, String raisedBy, String reason);
//
//    void suspendRelationship(Long relationshipId, String adminReason);
//
//    void completeRelationship(Long relationshipId);
//
//    void terminateRelationship(Long relationshipId, String reason);
}
