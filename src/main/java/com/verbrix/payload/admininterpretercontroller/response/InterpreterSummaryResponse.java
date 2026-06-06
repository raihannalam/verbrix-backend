package com.verbrix.payload.admininterpretercontroller.response;

import com.verbrix.model.enums.VerificationStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class InterpreterSummaryResponse {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private VerificationStatus status;
    private LocalDateTime createdAt;
}