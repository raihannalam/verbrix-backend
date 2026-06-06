package com.verbrix.payload.admininterpretercontroller.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.verbrix.model.enums.VerificationStatus;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CertificationAdminResponse {
    private Long id;
    private String name;
    private String issuingOrganization;
    private String fileUrl;
    private String description;
    private VerificationStatus status;
    private java.time.LocalDate issueDate;
    private java.time.LocalDate expiryDate;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String rejectionReason;
}