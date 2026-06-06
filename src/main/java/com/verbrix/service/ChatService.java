package com.verbrix.service;

import com.verbrix.model.chat.ChatMessage;
import com.verbrix.payload.chat.ChatRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ChatService {
    ChatMessage saveAndSend(String senderEmail, ChatRequest request);
    Page<ChatMessage> getChatHistory(Long relationshipId, String userEmail, Pageable pageable);

    // 🟢 NEW METHOD: Called by PaymentServiceImpl
    void sendSystemMessage(Long relationshipId, String content);
}