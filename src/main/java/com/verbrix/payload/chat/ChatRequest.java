package com.verbrix.payload.chat;

import lombok.Data;

@Data
public class ChatRequest {
    private Long relationshipId; // Which conversation does this belong to?
    private String content;      // "Hello, are you available?"
    private String type;
    private String fileUrl;   // "TEXT" (Default) or "IMAGE"
}