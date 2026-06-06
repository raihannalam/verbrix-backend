package com.verbrix.payload.publiccontroller.response;

import com.verbrix.model.enums.MedicalSpecialization;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

@Getter
@Builder
public class InterpreterPublicResponse {

    private Long id;
    private String firstName;
    private String lastName;
    private String bio;

    private String profilePictureUrl;
    private String introVideoUrl;

    private Integer experienceYears;
    private Integer experienceMonths;

    private Set<MedicalSpecialization> specializations;
    private List<LanguagePublicResponse> languages;

    private BigDecimal consultationFees;

    private BigDecimal rating;
    private Integer ratingCount;

    // coarse presence only
    private boolean online;
}
