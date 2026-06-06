package com.verbrix.service.helpers.interpreteradminservice;

import com.verbrix.model.profile.Interpreter;
import com.verbrix.model.requirement.Certification;
import com.verbrix.payload.admininterpretercontroller.response.CertificationAdminResponse;
import com.verbrix.payload.admininterpretercontroller.response.InterpreterDetailResponse;
import com.verbrix.payload.admininterpretercontroller.response.InterpreterSummaryResponse;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;

@Component
public class AdminMappingHelper {

    public InterpreterDetailResponse toDetailResponse(Interpreter interpreter) {

        var user = interpreter.getUser();

        return InterpreterDetailResponse.builder()
                // ─── Identity ─────────────────────────────
                .id(interpreter.getId())
                .firstName(interpreter.getFirstName())
                .lastName(interpreter.getLastName())
                .bio(interpreter.getBio())

                // ─── Experience ───────────────────────────
                .experienceYears(interpreter.getExperienceYears())
                .experienceMonths(interpreter.getExperienceMonths())

                // ─── User Account ─────────────────────────
                .userId(user.getId())
                .email(user.getEmail())

                // ─── Verification Assets ──────────────────
                .profilePictureUrl(interpreter.getProfilePictureUrl())
                .governmentIdType(interpreter.getGovernmentIdType())
                .governmentIdDetails(interpreter.getGovernmentIdDetails())
                .governmentIdUrl(interpreter.getGovernmentIdUrl())
                .introVideoUrl(interpreter.getIntroVideoUrl())

                // ─── Status & Availability ────────────────
                .status(interpreter.getStatus())
                .available(interpreter.isAvailable())
                .online(interpreter.isOnline())
                .lastSeenAt(interpreter.getLastSeenAt())
                .rejectionReason(interpreter.getRejectionReason())

                // ─── Skills & Qualifications ──────────────
                .languageAbilities(interpreter.getLanguageAbilities())
                .specializations(interpreter.getSpecializations())
                .certifications(
                        interpreter.getCertifications() != null
                                ? interpreter.getCertifications()
                                .stream()
                                .map(this::mapToCertificationAdminResponse)
                                .toList()
                                : new ArrayList<>()
                )

                .rating(interpreter.getRating())
                .ratingCount(interpreter.getRatingCount())

                // ─── Financials ───────────────────────────
                .consultationFee(interpreter.getConsultationFee())
                .serviceAgreementFee(interpreter.getServiceAgreementFee())
                .recurringFeeAmount(interpreter.getRecurringFeeAmount())
                .recurringFeeFrequency(interpreter.getRecurringFeeFrequency())

                // ─── Audit ────────────────────────────────
                .createdAt(interpreter.getCreatedAt())
                .lastModified(interpreter.getLastModified())
                .lastModifiedByEmail(
                        interpreter.getLastModifiedBy() != null
                                ? interpreter.getLastModifiedBy().getEmail()
                                : null
                )
                .build();
    }

    public InterpreterSummaryResponse toSummaryResponse(Interpreter interpreter) {
        return InterpreterSummaryResponse.builder()
                .id(interpreter.getId())
                .firstName(interpreter.getFirstName())
                .lastName(interpreter.getLastName())
                .email(interpreter.getUser().getEmail())
                .status(interpreter.getStatus())
                .createdAt(interpreter.getCreatedAt())
                .build();
    }

    private CertificationAdminResponse mapToCertificationAdminResponse(Certification cert) {
        return CertificationAdminResponse.builder()
                .id(cert.getId())
                .name(cert.getName())
                .issuingOrganization(cert.getIssuingOrganization())
                .fileUrl(cert.getFileUrl())
                .description(cert.getDescription())
                .status(cert.getStatus())
                .rejectionReason(cert.getRejectionReason())
                .issueDate(cert.getIssueDate())
                .expiryDate(cert.getExpiryDate())
                .build();
    }
}
