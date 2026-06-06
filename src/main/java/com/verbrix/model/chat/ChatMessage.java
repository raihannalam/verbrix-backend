package com.verbrix.model.chat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "chat_messages")
public class ChatMessage {

    @Id
    private String id;

    @Field("relationship_id")
    private Long relationshipId;

    @Field("sender_id")
    private Long senderId;

    @Field("sender_email")          // ✅ ADD
    private String senderEmail;

    @Field("recipient_id")
    private Long recipientId;

    private String fileUrl;

    private String content;

    @Builder.Default
    private MessageType type = MessageType.TEXT;

    @Builder.Default
    @Field("is_read")
    private boolean isRead = false;

    private Instant timestamp;

    public enum MessageType {
        TEXT, IMAGE, FILE, SYSTEM
    }
}
