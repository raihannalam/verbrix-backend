package com.verbrix.payload.relationship;

import com.verbrix.model.enums.RelationshipStatus;
import lombok.Builder;
import lombok.Data;
import java.time.Instant;

@Data
@Builder
public class RelationshipResponse {
    private Long relationshipId;
    private Long clientId;
    private String clientName;
    private Long interpreterId;
    private String interpreterName;
    private RelationshipStatus status;
    private String initialMessage;
    private Instant createdAt;
}