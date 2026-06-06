package com.verbrix.service.helpers.interpreterservice;

import com.verbrix.model.enums.VerificationStatus;
import com.verbrix.model.profile.Interpreter;
import com.verbrix.model.requirement.Certification;
import com.verbrix.model.requirement.LanguageAbility;
import com.verbrix.payload.interpretercontroller.request.CertificationRequest;
import com.verbrix.payload.interpretercontroller.request.InterpreterApplicationRequest;
import com.verbrix.payload.interpretercontroller.request.LanguageAbilityRequest;
import com.verbrix.payload.interpretercontroller.response.InterpreterApplicationResponse;
import com.verbrix.service.StorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class InterpreterMappingHelper {

    private final StorageService storageService;

    // --- 1. Request to Entity (Used during Apply/Re-Apply) ---
    public void mapApplicationRequest(Interpreter interpreter, InterpreterApplicationRequest request) {
        interpreter.setFirstName(request.getFirstName());
        interpreter.setLastName(request.getLastName());
        interpreter.setBio(request.getBio());
        interpreter.setProfilePictureUrl(request.getProfilePictureUrl());
        interpreter.setGovernmentIdUrl(request.getGovernmentIdUrl());
        interpreter.setGovernmentIdType(request.getGovernmentIdType());
        interpreter.setGovernmentIdDetails(request.getGovernmentIdDetails());
        interpreter.setExperienceYears(request.getExperienceYears());
        interpreter.setExperienceMonths(request.getExperienceMonths());
        interpreter.setIntroVideoUrl(request.getIntroVideoUrl());
        interpreter.setConsultationFee(request.getConsultationFee());
        interpreter.setServiceAgreementFee(request.getServiceAgreementFee());

        // Set Specializations
        if (request.getSpecializations() != null) {
            interpreter.setSpecializations(new HashSet<>(request.getSpecializations()));
        }

        // Set Languages
        if (request.getLanguageAbilities() != null) {
            List<LanguageAbility> abilities = request.getLanguageAbilities().stream()
                    .map(req -> LanguageAbility.builder()
                            .language(req.getLanguage())
                            .proficiency(req.getProficiency())
                            .proofUrl(req.getProofUrl().isEmpty() ? null : req.getProofUrl())
                            .build())
                    .collect(Collectors.toList());
            interpreter.setLanguageAbilities(abilities);
        }

        replaceCertifications(interpreter, request);
    }

    public void replaceCertifications(Interpreter interpreter, InterpreterApplicationRequest request) {
        if (interpreter.getCertifications() == null) {
            interpreter.setCertifications(new ArrayList<>());
        } else {
            interpreter.getCertifications().clear();
        }

        if (request.getCertifications() != null && !request.getCertifications().isEmpty()) {
            List<Certification> newCerts = request.getCertifications().stream()
                    .map(req -> Certification.builder()
                            .name(req.getName())
                            .issuingOrganization(req.getIssuingOrganization())
                            .fileUrl(req.getFileUrl())
                            .description(req.getDescription())
                            .issueDate(req.getIssueDate())
                            .expiryDate(req.getExpiryDate())
                            .status(VerificationStatus.PENDING)
                            .interpreter(interpreter)
                            .build())
                    .toList();
            interpreter.getCertifications().addAll(newCerts);
        }
    }

    // --- 2. Entity to Response (CRITICAL FOR PRE-FILLING FORM) ---
    public InterpreterApplicationResponse toApplicationResponse(Interpreter interpreter) {
        return InterpreterApplicationResponse.builder()
                .id(interpreter.getId())
                .status(interpreter.getStatus())
                .available(interpreter.isAvailable())
                .rejectionReason(interpreter.getRejectionReason())

                // A. Personal Info
                .firstName(interpreter.getFirstName())
                .lastName(interpreter.getLastName())
                .bio(interpreter.getBio())
                .profilePictureUrl(interpreter.getProfilePictureUrl())

                // B. Experience & Media
                .introVideoUrl(interpreter.getIntroVideoUrl())
                .experienceYears(interpreter.getExperienceYears())
                .experienceMonths(interpreter.getExperienceMonths())

                // C. Identity
                .governmentIdUrl(interpreter.getGovernmentIdUrl())
                .governmentIdType(interpreter.getGovernmentIdType())
                .governmentIdDetails(interpreter.getGovernmentIdDetails())

                // D. Financials
                .consultationFee(interpreter.getConsultationFee())
                .serviceAgreementFee(interpreter.getServiceAgreementFee())

                // E. Collections (Convert Entities back to Requests/DTOs)
                .specializations(interpreter.getSpecializations() != null
                        ? new HashSet<>(interpreter.getSpecializations())
                        : new HashSet<>())

                .languageAbilities(interpreter.getLanguageAbilities() != null
                        ? interpreter.getLanguageAbilities().stream()
                        .map(lang -> LanguageAbilityRequest.builder()
                                .language(lang.getLanguage())
                                .proficiency(lang.getProficiency())
                                .proofUrl(lang.getProofUrl())
                                .build())
                        .collect(Collectors.toList())
                        : new ArrayList<>())

                .certifications(interpreter.getCertifications() != null
                        ? interpreter.getCertifications().stream()
                        .map(cert -> CertificationRequest.builder()
                                .name(cert.getName())
                                .issuingOrganization(cert.getIssuingOrganization())
                                .description(cert.getDescription())
                                .fileUrl(cert.getFileUrl())
                                .issueDate(cert.getIssueDate())
                                .expiryDate(cert.getExpiryDate())
                                .build())
                        .collect(Collectors.toList())
                        : new ArrayList<>())

                .build();
    }

    public void confirmSafely(String url) {
        if (url != null && !url.isBlank()) {
            try {
                storageService.confirmFile(url);
            } catch (Exception e) {
                log.error("Failed to confirm file persistence for URL: {}", url, e);
            }
        }
    }
}