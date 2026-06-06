package com.verbrix.payload.admininterpretercontroller.response;

import com.verbrix.model.enums.GovernmentIdType;
import com.verbrix.model.enums.MedicalSpecialization;
import com.verbrix.model.enums.RecurringFeeFrequency;
import com.verbrix.model.enums.VerificationStatus;
import com.verbrix.model.requirement.LanguageAbility;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Data
@Builder
public class InterpreterDetailResponse {

    // ─── Identity ───────────────────────────────────────────────
    private Long id;
    private String firstName;
    private String lastName;
    private String bio;

    // ─── Experience ─────────────────────────────────────────────
    private Integer experienceYears;
    private Integer experienceMonths;

    // ─── User Account (RBAC) ────────────────────────────────────
    private Long userId;
    private String email;

    // ─── Verification Assets ────────────────────────────────────
    private String profilePictureUrl;
    private GovernmentIdType governmentIdType;
    private String governmentIdDetails;
    private String governmentIdUrl;
    private String introVideoUrl;

    // ─── Status & Availability ──────────────────────────────────
    private VerificationStatus status;
    private boolean available;
    private boolean online;
    private Instant lastSeenAt;
    private String rejectionReason;

    // ─── Skills & Qualifications ────────────────────────────────
    private List<LanguageAbility> languageAbilities;
    private Set<MedicalSpecialization> specializations;
    private List<CertificationAdminResponse> certifications;

    // ─── Ratings ────────────────────────────────────────────────
    private Integer ratingCount;
    private BigDecimal rating;

    // ─── Financials ─────────────────────────────────────────────
    private BigDecimal consultationFee;
    private BigDecimal serviceAgreementFee;
    private BigDecimal recurringFeeAmount;
    private RecurringFeeFrequency recurringFeeFrequency;

    // ─── Audit ──────────────────────────────────────────────────
    private LocalDateTime createdAt;
    private LocalDateTime lastModified;
    private String lastModifiedByEmail;
}