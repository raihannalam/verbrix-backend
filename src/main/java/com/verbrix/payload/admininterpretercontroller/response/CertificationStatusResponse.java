package com.verbrix.payload.admininterpretercontroller.response;

import com.verbrix.model.enums.VerificationStatus;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CertificationStatusResponse {
    private Long certificationId;
    private VerificationStatus status;
    private String adminNote;
}