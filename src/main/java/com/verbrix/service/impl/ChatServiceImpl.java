package com.verbrix.service.impl;

import com.verbrix.exception.ResourceNotFoundException;
import com.verbrix.model.business.booking.Relationship;
import com.verbrix.model.chat.ChatMessage;
import com.verbrix.model.enums.RelationshipStatus;
import com.verbrix.model.rbac.User;
import com.verbrix.payload.chat.ChatRequest;
import com.verbrix.repository.ChatMessageRepository;
import com.verbrix.repository.RelationshipRepository;
import com.verbrix.repository.UserRepository;
import com.verbrix.service.ChatService;
import com.verbrix.service.RealtimeEventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatServiceImpl implements ChatService {

    private final ChatMessageRepository chatMessageRepository;
    private final UserRepository userRepository;
    private final RelationshipRepository relationshipRepository;
    private final RealtimeEventPublisher eventPublisher;

    @Override
    @Transactional(readOnly = true, transactionManager = "transactionManager")
    public ChatMessage saveAndSend(String senderEmail, ChatRequest request) throws AccessDeniedException {
        User sender = userRepository.findByEmail(senderEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // 🟢 Fetch Relationship Eagerly
        Relationship rel = relationshipRepository.findRelationshipWithUsers(request.getRelationshipId())
                .orElseThrow(() -> new ResourceNotFoundException("Relationship not found"));

        boolean isClient = rel.getClient().getId().equals(sender.getId());
        boolean isInterpreter = rel.getInterpreter().getId().equals(sender.getId());

        if (!isClient && !isInterpreter) {
            throw new AccessDeniedException("You are not a participant in this conversation");
        }

        // 🟢 Block chat if status is invalid (e.g., Terminated), but allow if Active
        if (rel.getStatus() == RelationshipStatus.SUSPENDED ||
                rel.getStatus() == RelationshipStatus.TERMINATED) {
            throw new AccessDeniedException("You cannot chat in this state: " + rel.getStatus());
        }

        Long recipientId = isClient ? rel.getInterpreter().getId() : rel.getClient().getId();
        String recipientEmail = isClient ? rel.getInterpreter().getEmail() : rel.getClient().getEmail();

        ChatMessage.MessageType type;
        try {
            type = request.getType() != null
                    ? ChatMessage.MessageType.valueOf(request.getType())
                    : ChatMessage.MessageType.TEXT;
        } catch (IllegalArgumentException e) {
            type = ChatMessage.MessageType.TEXT;
        }

        ChatMessage chatMessage = ChatMessage.builder()
                .relationshipId(rel.getId())
                .senderId(sender.getId())
                .senderEmail(sender.getEmail())
                .recipientId(recipientId)
                .fileUrl(request.getFileUrl())
                .content(request.getContent())
                .type(type)
                .timestamp(Instant.now())
                .isRead(false)
                .build();

        ChatMessage saved = chatMessageRepository.save(chatMessage);

        // 🟢 Send to Specific User Queue (for Notifications)
        eventPublisher.sendToUser(
                recipientEmail,
                "CHAT_MESSAGE_RECEIVED",
                Map.of(
                        "id", saved.getId(),
                        "content", saved.getContent() != null ? saved.getContent() : "",
                        "senderId", saved.getSenderId(),
                        "senderEmail", saved.getSenderEmail(),
                        "relationshipId", saved.getRelationshipId(),
                        "timestamp", saved.getTimestamp().toString(),
                        "type", saved.getType().name(),
                        "fileUrl", saved.getFileUrl() != null ? saved.getFileUrl() : ""
                )
        );

        // 🟢 Broadcast to Topic (for Live Chat UI)
        eventPublisher.sendToGroup("/topic/chat/" + saved.getRelationshipId(), saved);

        log.info("Message sent from {} to relationship {}", senderEmail, rel.getId());
        return saved;
    }

    @Override
    @Transactional(readOnly = true, transactionManager = "transactionManager")
    public Page<ChatMessage> getChatHistory(Long relationshipId, String userEmail, Pageable pageable) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Relationship rel = relationshipRepository.findRelationshipWithUsers(relationshipId)
                .orElseThrow(() -> new ResourceNotFoundException("Relationship not found"));

        boolean isParticipant = rel.getClient().getId().equals(user.getId()) ||
                rel.getInterpreter().getId().equals(user.getId());

        if (!isParticipant) {
            throw new AccessDeniedException("You are not a participant of this conversation.");
        }

        return chatMessageRepository.findByRelationshipIdOrderByTimestampDesc(relationshipId, pageable);
    }

    // 🟢 NEW METHOD: Called by PaymentServiceImpl
    @Override
    public void sendSystemMessage(Long relationshipId, String content) {
        ChatMessage systemMsg = ChatMessage.builder()
                .relationshipId(relationshipId)
                .senderId(0L) // System ID
                .senderEmail("VERBRIX_SYSTEM")
                .content(content)
                .type(ChatMessage.MessageType.SYSTEM) // Ensure Enum has SYSTEM
                .timestamp(Instant.now())
                .isRead(true)
                .build();

        ChatMessage saved = chatMessageRepository.save(systemMsg);

        // Broadcast to the Chat Room so both users see it instantly
        eventPublisher.sendToGroup("/topic/chat/" + relationshipId, saved);

        log.info("System message sent to relationship {}", relationshipId);
    }
}