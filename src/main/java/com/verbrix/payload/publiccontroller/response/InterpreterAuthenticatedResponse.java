package com.verbrix.payload.publiccontroller.response;

import com.verbrix.model.enums.MedicalSpecialization;
import com.verbrix.model.requirement.Certification;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Set;

@Getter
@Builder
public class InterpreterAuthenticatedResponse {

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

    // booking-relevant
    private boolean online;
    private boolean available;

    // allowed only after login
    private Instant lastSeenAt;
}
