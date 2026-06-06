package com.verbrix.payload.interpretercontroller.request;

import com.verbrix.model.enums.GovernmentIdType;
import com.verbrix.model.enums.MedicalSpecialization;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InterpreterApplicationRequest {

    @NotBlank
    private String firstName;

    @NotBlank
    private String lastName;

    @NotBlank
    private String bio;

    @NotBlank
    private String profilePictureUrl;

    @NotBlank
    private String governmentIdUrl;

    @Enumerated(EnumType.STRING)
    private GovernmentIdType governmentIdType;

    private String governmentIdDetails;

    @NotBlank
    private String introVideoUrl;

    private Integer experienceMonths;
    private Integer experienceYears;

    @NotEmpty
    @Valid
    private List<LanguageAbilityRequest> languageAbilities;

    @NotEmpty
    private Set<MedicalSpecialization> specializations;

    @Valid
    private List<CertificationRequest> certifications;

    @Column(precision = 10, scale = 2)
    private BigDecimal consultationFee;

    @Column(precision = 10, scale = 2)
    private BigDecimal serviceAgreementFee;

}
