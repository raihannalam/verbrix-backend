package com.verbrix.repository;

import com.verbrix.model.chat.ChatMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatMessageRepository extends MongoRepository<ChatMessage, String> {

    // 1. Load History
    List<ChatMessage> findByRelationshipIdOrderByTimestampAsc(Long relationshipId);

    // 2. Count Unread (Optional)
    long countByRecipientIdAndRelationshipIdAndIsReadFalse(Long recipientId, Long relationshipId);

    Page<ChatMessage> findByRelationshipIdOrderByTimestampDesc(Long relationshipId,
                                                               Pageable pageable);
}