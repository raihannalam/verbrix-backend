package com.verbrix.controller;

import com.verbrix.model.chat.ChatMessage;
import com.verbrix.payload.chat.ChatRequest;
import com.verbrix.service.ChatService;
import com.verbrix.service.RealtimeEventPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;
    private final RealtimeEventPublisher eventPublisher;

    /**
     * WEBSOCKET ENDPOINT
     * usage: stompClient.send("/app/chat.sendMessage", ...)
     */
    @MessageMapping("/chat.sendMessage")
    public void sendMessage(@Payload ChatRequest request, Principal principal) {
        // 1. Save to DB (Service Layer)
        ChatMessage saved = chatService.saveAndSend(principal.getName(), request);

        // 2. Broadcast to subscribers (e.g. /topic/chat/100)
        eventPublisher.sendToGroup("/topic/chat/" + saved.getRelationshipId(), saved);
    }

    /**
     * HTTP ENDPOINT (REST)
     * usage: GET /api/v1/chat/{id}/history
     * Missing this caused your 404/500 error!
     */
    @GetMapping("/api/v1/chat/{relationshipId}/history")
    @ResponseBody // <--- CRITICAL: Tells Spring this is JSON, not a HTML view
    public ResponseEntity<Page<ChatMessage>> getChatHistory(
            @PathVariable Long relationshipId,
            @AuthenticationPrincipal UserDetails userDetails,
            @PageableDefault(size = 20, sort = "timestamp", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ResponseEntity.ok(
                chatService.getChatHistory(relationshipId, userDetails.getUsername(), pageable)
        );
    }
}