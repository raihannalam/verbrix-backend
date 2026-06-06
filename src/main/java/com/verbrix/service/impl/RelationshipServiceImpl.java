package com.verbrix.service.impl;

import com.verbrix.exception.ConflictException;
import com.verbrix.exception.ResourceNotFoundException;
import com.verbrix.model.business.booking.Relationship;
import com.verbrix.model.business.payment.Subscription;
import com.verbrix.model.business.payment.Transaction;
import com.verbrix.model.chat.ChatMessage;
import com.verbrix.model.enums.*;
import com.verbrix.model.profile.Interpreter;
import com.verbrix.model.rbac.User;
import com.verbrix.payload.relationship.RelationshipActionRequest;
import com.verbrix.payload.relationship.RelationshipResponse;
import com.verbrix.repository.*;
import com.verbrix.service.PaymentService;
import com.verbrix.service.RealtimeEventPublisher;
import com.verbrix.service.RelationshipService;
import com.verbrix.service.helpers.relationshipservice.RelationshipServiceHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.List;
import java.util.Map;


@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class RelationshipServiceImpl implements RelationshipService {

    private final RelationshipRepository relationshipRepository;
    private final UserRepository userRepository;
    private final RelationshipServiceHelper relationshipServiceHelper;
    private final InterpreterRepository interpreterRepository;
    private final RealtimeEventPublisher eventPublisher;
    private final TransactionRepository transactionRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final PaymentService paymentService;
    private final SubscriptionRepository subscriptionRepository;


    @Override
    @Transactional
    public RelationshipResponse connect(String clientEmail, Long interpreterProfileId, String initialMessage) {
        User clientUser = userRepository.findByEmail(clientEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found."));

        Interpreter interpreterProfile = interpreterRepository.findById(interpreterProfileId)
                .orElseThrow(() -> new ResourceNotFoundException("Interpreter profile not found."));

        User interpreterUser = interpreterProfile.getUser();

        if (clientUser.getId().equals(interpreterUser.getId())) {
            throw new ConflictException("You cannot connect to yourself.");
        }
        boolean isInterpreter = interpreterUser.getRoles().stream()
                .anyMatch(role -> role.getName() == RoleType.INTERPRETER);
        if (!isInterpreter) {
            throw new ConflictException("User is not an interpreter.");
        }

        List<RelationshipStatus> activeStatuses = List.of(
                RelationshipStatus.REQUEST_ACCEPTED,
                RelationshipStatus.CONSULTATION_PENDING_PAYMENT,
                RelationshipStatus.CONSULTATION_ACTIVE,
                RelationshipStatus.AGREEMENT_ACTIVE,
                RelationshipStatus.WORK_ACTIVE
        );
        if (relationshipRepository.existsByClientAndInterpreterAndStatusIn(
                clientUser,
                interpreterUser,
                activeStatuses
        )) {
            throw new ConflictException("You already have an active connection with this interpreter.");
        }
        if (relationshipRepository.existsByClientAndInterpreterAndStatus(
                clientUser, interpreterUser, RelationshipStatus.REQUESTED
        )) {
            throw new ConflictException("Connection request already sent.");
        }

        Relationship relationship = Relationship.builder()
                .client(clientUser)
                .interpreter(interpreterUser)
                .status(RelationshipStatus.REQUESTED)
                .initialMessage(initialMessage)
                .build();

        Relationship savedRel = relationshipRepository.save(relationship);

        ChatMessage firstMsg = ChatMessage.builder()
                .relationshipId(savedRel.getId())
                .senderId(clientUser.getId())
                .senderEmail(clientUser.getEmail())
                .recipientId(interpreterUser.getId())
                .content(initialMessage)
                .type(ChatMessage.MessageType.TEXT)
                .timestamp(Instant.now())
                .isRead(false)
                .build();

        chatMessageRepository.save(firstMsg);

        eventPublisher.sendToUser(
                interpreterUser.getEmail(),
                "RELATIONSHIP_REQUEST_RECEIVED",
                Map.of(
                        "relationshipId", savedRel.getId(),
                        "clientName", clientUser.getClientProfile().getFirstName() + " " + clientUser.getClientProfile().getLastName(),
                        "initialMessage", initialMessage,
                        "chatMessageId", firstMsg.getId()
                        )
        );

        return relationshipServiceHelper.toResponse(savedRel);
    }

    @Override
    public List<RelationshipResponse> getIncomingRequests(String interpreterEmail) {
        User interpreterUser = userRepository.findByEmail(interpreterEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found."));
        interpreterRepository.findByUser(interpreterUser)
                .orElseThrow(() -> new ResourceNotFoundException("Interpreter profile not found."));

        return relationshipRepository
                .findAllByInterpreterAndStatus(
                        interpreterUser,
                        RelationshipStatus.REQUESTED
                )
                .stream()
                .map(relationshipServiceHelper::toResponse)
                .toList();
    }

    @Override
    public RelationshipResponse respondToConnectionRequest(Long relationshipId, String interpreterEmail, RelationshipActionRequest request) {
        User interpreterUser = userRepository.findByEmail(interpreterEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found."));

        Relationship relationship = relationshipRepository.findById(relationshipId)
                .orElseThrow(() -> new ResourceNotFoundException("Relationship not found."));


        if (!relationship.getInterpreter().getId().equals(interpreterUser.getId())) {
            throw new ConflictException("Not allowed to respond to this request.");
        }
        if (relationship.getStatus() != RelationshipStatus.REQUESTED) {
            throw new ConflictException("Cannot respond to this request.");
        }
        RelationshipStatus newStatus = request.getAction() == RelationshipActionRequest.Action.ACCEPT
        ? RelationshipStatus.REQUEST_ACCEPTED
                : RelationshipStatus.TERMINATED;

        relationship.setStatus(newStatus);
        Relationship saved = relationshipRepository.save(relationship);

        eventPublisher.sendToUser(
                relationship.getClient().getEmail(),
                "RELATIONSHIP_STATUS_UPDATE",
                Map.of(
                        "relationshipId", saved.getId(),
                        "status", newStatus.name(),
                        "interpreterName", relationship.getInterpreter().getInterpreterProfile().getFirstName(),
                        "timestamp", Instant.now().toString()
                )
        );

return relationshipServiceHelper.toResponse(saved);


    }

    @Override
    @Transactional
    public void onPaymentSuccess(Long transactionId) {

        Transaction transaction = transactionRepository.findByIdWithRelationship(transactionId)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found"));

        Relationship relationship = transaction.getRelationship();
        RelationshipStatus status = relationship.getStatus(); // ✅ safe

        switch (transaction.getPurpose()) {

            case TransactionPurpose.CONSULTATION_FEE ->
                    handleConsultationFeeSuccess(relationship, transaction);
            case TransactionPurpose.SERVICE_AGREEMENT_FEE ->
                    handleServiceAgreementFeeSuccess(relationship, transaction);
            case TransactionPurpose.RECURRING_FEES ->
                    handleRecurringFeeSuccess(relationship, transaction);

            default ->
                    throw new IllegalStateException(
                            "Unknown transaction purpose: " + transaction.getPurpose()
                    );
        }
    }


    private void handleConsultationFeeSuccess(Relationship relationship, Transaction transaction) {

        RelationshipStatus status = relationship.getStatus();


        if (status == RelationshipStatus.CONSULTATION_ACTIVE) {
            log.warn("Duplicate payment for relationship: {}", relationship.getId());

            paymentService.refundTransaction(transaction.getId());

            return;
        }

        if (status != RelationshipStatus.REQUEST_ACCEPTED &&
                status != RelationshipStatus.CONSULTATION_PENDING_PAYMENT) {
            throw new ConflictException("Consultation fee not allowed in state: " + status);
        }

        BigDecimal expectedAmount =
                relationshipServiceHelper.resolveConsultationFeeHelper(relationship);

        if (transaction.getAmount().compareTo(expectedAmount) != 0) {
            throw new ConflictException("Incorrect consultation fee amount");
        }

        relationship.setStatus(RelationshipStatus.CONSULTATION_ACTIVE);
        relationship.setConsultationStartedAt(Instant.now());

        relationshipRepository.save(relationship);
        Map<String, Object> payload = Map.of(
                "relationshipId", relationship.getId(),
                "status", RelationshipStatus.CONSULTATION_ACTIVE.name(),
                "timestamp", Instant.now().toString()

        );
        eventPublisher.sendToUser(
                relationship.getClient().getEmail(),
                "CONSULTATION_STARTED",
                payload);

        eventPublisher.sendToUser(
                relationship.getInterpreter().getEmail(),
                "CONSULTATION_STARTED",
                payload
        );
    }


    private void handleServiceAgreementFeeSuccess(Relationship relationship, Transaction transaction) {
        RelationshipStatus status = relationship.getStatus();
        if (status == RelationshipStatus.AGREEMENT_ACTIVE) {
            return;
        }

        if (status != RelationshipStatus.CONSULTATION_COMPLETED) {
            throw new ConflictException("Service agreement fee payment not allowed in state: " + status);
        }

        BigDecimal expectedAmount = relationshipServiceHelper.resolveServiceAgreementFeeHelper((relationship));
        if (transaction.getAmount() == null || transaction.getAmount().compareTo(expectedAmount) != 0) {
            throw new ConflictException("Incorrect amount for service agreement fee payment. Expected: " + expectedAmount + ", Actual: " + transaction.getAmount());
        }
        relationship.setStatus(RelationshipStatus.AGREEMENT_ACTIVE);
        relationshipRepository.save(relationship);

    }

    private void handleRecurringFeeSuccess(Relationship relationship, Transaction transaction) {
        String subId = transaction.getRazorpaySubscriptionId();

        Subscription subscription = subscriptionRepository.findByRazorpaySubscriptionId(subId)
                .orElseThrow(() -> new ResourceNotFoundException("Subscription not found"));

        Instant newPeriodEnd = calculateNewPeriodEnd(subscription.getFrequency(),
                subscription.getCurrentPeriodEnd());

        subscription.setStatus(SubscriptionStatus.ACTIVE);
        subscription.setCurrentPeriodEnd(newPeriodEnd);
        subscription.setSuccessfulChargeCount(subscription.getSuccessfulChargeCount() + 1);
        subscriptionRepository.save(subscription);

        if (relationship.getStatus() == RelationshipStatus.WORK_PAUSED) {
            relationship.setStatus(RelationshipStatus.WORK_ACTIVE);
            relationshipRepository.save(relationship);
        }
        eventPublisher.sendToUser(relationship.getClient().getEmail(), "SUBSCRIPTION_PAID",
                Map.of("newEndDate", newPeriodEnd.toString()));

    }

    private Instant calculateNewPeriodEnd(RecurringFeeFrequency frequency, Instant currentPeriodEnd) {
        Instant baseline = (currentPeriodEnd == null || currentPeriodEnd.isBefore(Instant.now()))
                ? Instant.now() : currentPeriodEnd;
        return switch (frequency) {
            case RecurringFeeFrequency.DAILY -> baseline.plus(1, ChronoUnit.DAYS);
            case RecurringFeeFrequency.WEEKLY -> baseline.plus(7, ChronoUnit.DAYS);
            case RecurringFeeFrequency.MONTHLY -> baseline.plus(30, ChronoUnit.DAYS);
        };
    }


    // In RelationshipServiceImpl.java

    @Override
    public List<RelationshipResponse> getMyActiveRelationships(String email) {
        // 1. Find the User (Standard RBAC check)
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found."));

        Collection<RelationshipStatus> activeStatuses = List.of(
                RelationshipStatus.REQUESTED,                   // Pending requests
                RelationshipStatus.REQUEST_ACCEPTED,            // Accepted, pre-consultation
                RelationshipStatus.CONSULTATION_PENDING_PAYMENT,// Payment flow
                RelationshipStatus.CONSULTATION_ACTIVE,         // Live Video/Chat
                RelationshipStatus.CONSULTATION_COMPLETED,      // Finished, pending next steps
                RelationshipStatus.AGREEMENT_ACTIVE             // Long-term agreement
        );

        List<Relationship> relationships;

        // 3. Determine Role and Query Efficiently
        // We check the User's roles (from your User entity definition)
        boolean isInterpreter = user.getRoles().stream()
                .anyMatch(r -> r.getName() == RoleType.INTERPRETER);

        if (isInterpreter) {
            // Uses the 'interpreter' field from Relationship entity
            relationships = relationshipRepository.findAllByInterpreterAndStatusIn(user, activeStatuses );
        } else {
            // Uses the 'client' field from Relationship entity
            relationships = relationshipRepository.findAllByClientAndStatusIn(user, activeStatuses);
        }

        // 4. Map to DTO
        return relationships.stream()
                .map(relationshipServiceHelper::toResponse)
                .toList();
    }
}

