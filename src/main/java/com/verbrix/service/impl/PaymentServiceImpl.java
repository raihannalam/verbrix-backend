package com.verbrix.service.impl;

import com.razorpay.*;
import com.verbrix.exception.ConflictException;
import com.verbrix.exception.ResourceNotFoundException;
import com.verbrix.model.business.booking.Relationship;
import com.verbrix.model.business.payment.Transaction;
import com.verbrix.model.enums.PaymentStatus;
import com.verbrix.model.enums.RelationshipStatus;
import com.verbrix.model.enums.TransactionPurpose;
import com.verbrix.model.profile.Interpreter;
import com.verbrix.model.rbac.User;
import com.verbrix.payload.payment.OrderResponse;
import com.verbrix.payload.payment.PaymentVerificationRequest;
import com.verbrix.payload.payment.response.TransactionResponse;
import com.verbrix.repository.InterpreterRepository;
import com.verbrix.repository.RelationshipRepository;
import com.verbrix.repository.TransactionRepository;
import com.verbrix.repository.UserRepository;
import com.verbrix.service.ChatService;
import com.verbrix.service.PaymentService;
import com.verbrix.service.RealtimeEventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentServiceImpl implements PaymentService {

    private final RelationshipRepository relationshipRepository;
    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final InterpreterRepository interpreterRepository;
    private final ChatService chatService;
    private final RealtimeEventPublisher eventPublisher;

    @Value("${razorpay.key.id}")
    private String razorpayKeyId;

    @Value("${razorpay.key.secret}")
    private String razorpayKeySecret;

    @Override
    @Transactional
    public OrderResponse createConsultationOrder(Long relationshipId, String userEmail) {

        User client = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Relationship relationship = relationshipRepository.findById(relationshipId)
                .orElseThrow(() -> new RuntimeException("Relationship not found"));

        if (!relationship.getClient().getId().equals(client.getId())) {
            throw new ConflictException("User is not a participant of this conversation");
        }

        // 1. SAFETY CHECK: Is the service already active?
        if (relationship.getStatus() == RelationshipStatus.CONSULTATION_ACTIVE) {
            throw new ConflictException("Consultation is already active. No payment needed.");
        }

        // 2. IDEMPOTENCY CHECK: Has a successful payment already been recorded?
        boolean alreadyPaid = transactionRepository.existsByRelationshipAndPurposeAndPaymentStatus(
                relationship,
                TransactionPurpose.CONSULTATION_FEE,
                PaymentStatus.SUCCESS
        );

        if (alreadyPaid) {
            throw new ConflictException("Payment already completed for this consultation.");
        }

        // 3. PENDING CHECK: Reuse existing pending order to avoid spamming Razorpay (Fixes 500 error)
        // We fetch a LIST ordered by newest first, so we don't crash if multiple exist.
        List<Transaction> pendingTxns = transactionRepository.findByRelationshipAndPurposeAndPaymentStatusOrderByCreatedAtDesc(
                relationship,
                TransactionPurpose.CONSULTATION_FEE,
                PaymentStatus.CREATED
        );

        if (!pendingTxns.isEmpty()) {
            Transaction existingTxn = pendingTxns.getFirst(); // Take the most recent one
            log.info("Resuming existing pending order: {} for relationship: {}", existingTxn.getRazorpayOrderId(), relationshipId);
            return OrderResponse.builder()
                    .razorpayOrderId(existingTxn.getRazorpayOrderId())
                    .amount(existingTxn.getAmount().multiply(new BigDecimal(100)).intValue())
                    .currency(existingTxn.getCurrency())
                    .keyId(razorpayKeyId)
                    .build();
        }

        // 4. CREATE NEW ORDER
        Interpreter interpreterProfile = interpreterRepository.findByUser(relationship.getInterpreter())
                .orElseThrow(() -> new ResourceNotFoundException("Interpreter not found"));

        BigDecimal fee = interpreterProfile.getConsultationFee();
        if (fee == null || fee.compareTo(BigDecimal.ZERO) == 0) {
            fee = BigDecimal.valueOf(50); // Default fallback fee
        }

        try {
            RazorpayClient razorpayClient = new RazorpayClient(razorpayKeyId, razorpayKeySecret);
            JSONObject orderRequest = new JSONObject();
            orderRequest.put("amount", fee.multiply(new BigDecimal(100)).intValue()); // paise
            orderRequest.put("currency", "INR");
            orderRequest.put("receipt", "txn_" + System.currentTimeMillis());

            JSONObject notes = new JSONObject();
            notes.put("relationship_id", relationship.getId().toString());
            notes.put("purpose", TransactionPurpose.CONSULTATION_FEE);
            orderRequest.put("notes", notes);

            Order razorpayOrder = razorpayClient.orders.create(orderRequest);

            Transaction transaction = Transaction.builder()
                    .relationship(relationship)
                    .razorpayOrderId(razorpayOrder.get("id"))
                    .amount(fee)
                    .currency("INR")
                    .purpose(TransactionPurpose.CONSULTATION_FEE)
                    .paymentStatus(PaymentStatus.CREATED)
                    .build();
            transactionRepository.save(transaction);

            return OrderResponse.builder()
                    .razorpayOrderId(razorpayOrder.get("id"))
                    .amount(fee.multiply(new BigDecimal(100)).intValue())
                    .currency("INR")
                    .keyId(razorpayKeyId)
                    .build();
        } catch (RazorpayException e) {
            log.error("Razorpay Error", e);
            throw new RuntimeException("Payment initiation failed. Please try again.");
        }
    }

    @Override
    @Transactional
    public void verifyPayment(PaymentVerificationRequest request) {
        try {
            // 1. Verify Signature
            JSONObject options = new JSONObject();
            options.put("razorpay_order_id", request.getRazorpayOrderId());
            options.put("razorpay_payment_id", request.getRazorpayPaymentId());
            options.put("razorpay_signature", request.getRazorpaySignature());

            boolean isValid = Utils.verifyPaymentSignature(options, razorpayKeySecret);

            if (!isValid) {
                throw new ConflictException("Invalid Payment Signature");
            }

            // 2. Find Transaction
            Transaction transaction = transactionRepository.findByRazorpayOrderId(request.getRazorpayOrderId())
                    .orElseThrow(() -> new ResourceNotFoundException("Transaction not found for order: " + request.getRazorpayOrderId()));

            // 3. IDEMPOTENCY: If this specific transaction is already success, return immediately.
            if (transaction.getPaymentStatus() == PaymentStatus.SUCCESS) {
                log.info("Transaction {} already verified. Skipping.", transaction.getId());
                return;
            }

            Relationship rel = transaction.getRelationship();

            // 4. DUPLICATE PAYMENT CHECK (Auto-Refund)
            // If the relationship is ALREADY active (paid by a different transaction), we refund this one.
            if (rel.getStatus() == RelationshipStatus.CONSULTATION_ACTIVE) {
                log.warn("Duplicate Payment Detected! Relationship {} is already active. Initiating Refund for Txn: {}", rel.getId(), transaction.getId());

                // Mark as success first (to record we received money), then refund.
                transaction.setRazorpayPaymentId(request.getRazorpayPaymentId());
                transaction.setPaymentStatus(PaymentStatus.SUCCESS);
                transactionRepository.saveAndFlush(transaction);

                this.refundTransaction(transaction.getId());
                return;
            }

            // 5. SUCCESS FLOW
            transaction.setRazorpayPaymentId(request.getRazorpayPaymentId());
            transaction.setPaymentStatus(PaymentStatus.SUCCESS);
            transactionRepository.save(transaction);

            if (transaction.getPurpose() == TransactionPurpose.CONSULTATION_FEE) {
                // Unlock the relationship
                rel.setStatus(RelationshipStatus.CONSULTATION_ACTIVE);
                relationshipRepository.save(rel);

                // Notify chat participants
                chatService.sendSystemMessage(
                        rel.getId(),
                        "Payment of ₹" + transaction.getAmount() + " received. Video calling is now unlocked."
                );

                eventPublisher.sendToGroup(
                        "/topic/chat/" + rel.getId(), // Ensure topic matches frontend subscription
                        Map.of(
                                "type", "STATUS_UPDATE",
                                "status", RelationshipStatus.CONSULTATION_ACTIVE.name(),
                                "relationshipId", rel.getId()
                        )
                );
            }

        } catch (RazorpayException e) {
            log.error("Payment Verification Failed", e);
            throw new RuntimeException("Payment verification failed", e);
        }
    }

    @Override
    public void refundTransaction(Long transactionId) {
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found"));

        if (transaction.getPaymentStatus() == PaymentStatus.REFUNDED) {
            return;
        }

        try {
            RazorpayClient razorpayClient = new RazorpayClient(razorpayKeyId, razorpayKeySecret);

            JSONObject refundRequest = new JSONObject();
            refundRequest.put("payment_id", transaction.getRazorpayPaymentId());
            // Amount must be in paise
            refundRequest.put("amount", transaction.getAmount().multiply(new BigDecimal(100)).intValue());

            JSONObject notes = new JSONObject();
            notes.put("reason", "Duplicate Payment Auto-Refund");
            refundRequest.put("notes", notes);

            razorpayClient.payments.refund(refundRequest);

            transaction.setPaymentStatus(PaymentStatus.REFUNDED);
            transactionRepository.save(transaction);
            log.info("Auto refunded duplicate transaction: {}", transactionId);

        } catch (RazorpayException e) {
            log.error("Payment Refund Failed for transaction: {}", transactionId, e);
            transaction.setPaymentStatus(PaymentStatus.REFUND_FAILED);
            transactionRepository.save(transaction);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TransactionResponse> getMyTransactions(String userEmail, Pageable pageable) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found."));

        Page<Transaction> transactionPage = transactionRepository.findAllByUserId(user.getId(), pageable);

        return transactionPage.map(txn -> {
            Relationship relationship = txn.getRelationship();
            String otherPartyName;

            // Determine name of the other person in the transaction
            if (relationship.getClient().getId().equals(user.getId())) {
                otherPartyName = relationship.getInterpreter().getInterpreterProfile().getFirstName() + " " + relationship.getInterpreter().getInterpreterProfile().getLastName();
            } else {
                otherPartyName = relationship.getClient().getClientProfile().getFirstName() + " " + relationship.getClient().getClientProfile().getLastName();
            }

            return TransactionResponse.builder()
                    .id(txn.getId())
                    .razorpayOrderId(txn.getRazorpayOrderId())
                    .razorpayPaymentId(txn.getRazorpayPaymentId())
                    .amount(txn.getAmount())
                    .currency(txn.getCurrency())
                    .purpose(txn.getPurpose())
                    .status(txn.getPaymentStatus())
                    .createdAt(txn.getCreatedAt())
                    .relationshipId(relationship.getId())
                    .otherPartyName(otherPartyName)
                    .build();
        });
    }
}