package com.verbrix.payload.interpretercontroller.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.verbrix.model.enums.GovernmentIdType;
import com.verbrix.model.enums.MedicalSpecialization;
import com.verbrix.model.enums.VerificationStatus;
import com.verbrix.payload.interpretercontroller.request.CertificationRequest;
import com.verbrix.payload.interpretercontroller.request.LanguageAbilityRequest;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class InterpreterApplicationResponse {

    private Long id;
    private VerificationStatus status;
    private boolean available;
    private String rejectionReason;

    // --- Personal Information (CRITICAL: These must be here!) ---
    private String firstName;
    private String lastName;
    private String bio;
    private String profilePictureUrl;

    // --- Experience & Media ---
    private Integer experienceYears;
    private Integer experienceMonths;
    private String introVideoUrl;

    // --- Identity Verification ---
    private GovernmentIdType governmentIdType;
    private String governmentIdDetails;
    private String governmentIdUrl;

    // --- Financials ---
    private BigDecimal consultationFee;
    private BigDecimal serviceAgreementFee;

    // --- Collections ---
    private Set<MedicalSpecialization> specializations;
    private List<LanguageAbilityRequest> languageAbilities;
    private List<CertificationRequest> certifications;
}