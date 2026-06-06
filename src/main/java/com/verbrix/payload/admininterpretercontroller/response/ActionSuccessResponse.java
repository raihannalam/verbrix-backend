package com.verbrix.payload.admininterpretercontroller.response;

import com.verbrix.model.enums.VerificationStatus;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class ActionSuccessResponse {
    private Long interpreterId;
    private VerificationStatus newStatus;
    private String message;
    private Instant timestamp;
}