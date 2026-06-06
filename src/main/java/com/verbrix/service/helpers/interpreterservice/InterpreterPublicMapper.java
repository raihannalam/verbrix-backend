package com.verbrix.service.helpers.interpreterservice;

import com.verbrix.model.profile.Interpreter;
import com.verbrix.payload.publiccontroller.response.InterpreterAuthenticatedResponse;
import com.verbrix.payload.publiccontroller.response.InterpreterPublicResponse;
import com.verbrix.payload.publiccontroller.response.LanguagePublicResponse;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

@Component
public class InterpreterPublicMapper {

    public InterpreterPublicResponse toPublic(Interpreter interpreter) {
        return InterpreterPublicResponse.builder()
                .id(interpreter.getId())
                .firstName(interpreter.getFirstName())
                .lastName(interpreter.getLastName())
                .bio(interpreter.getBio())
                .profilePictureUrl(interpreter.getProfilePictureUrl())
                .introVideoUrl(interpreter.getIntroVideoUrl())
                .experienceYears(interpreter.getExperienceYears())
                .experienceMonths(interpreter.getExperienceMonths())
                .consultationFees(interpreter.getConsultationFee())
                .specializations(interpreter.getSpecializations())
                .languages(
                        interpreter.getLanguageAbilities().stream()
                                .map(la -> LanguagePublicResponse.builder()
                                        .language(la.getLanguage())
                                        .proficiency(la.getProficiency())
                                        .build())
                                .toList()
                )
                .rating(interpreter.getRating())
                .ratingCount(interpreter.getRatingCount())
                .online(interpreter.isOnline())
                .build();
    }


    public InterpreterAuthenticatedResponse toAuthenticated(Interpreter interpreter) {
        return InterpreterAuthenticatedResponse.builder()
                .id(interpreter.getId())
                .firstName(interpreter.getFirstName())
                .lastName(interpreter.getLastName())
                .bio(interpreter.getBio())
                .profilePictureUrl(interpreter.getProfilePictureUrl())
                .introVideoUrl(interpreter.getIntroVideoUrl())
                .experienceYears(interpreter.getExperienceYears())
                .experienceMonths(interpreter.getExperienceMonths())
                .specializations(interpreter.getSpecializations() != null ?
                        interpreter.getSpecializations() : Collections.emptySet())

                .languages(interpreter.getLanguageAbilities() == null ?
                        List.of()
                        : interpreter.getLanguageAbilities()
                        .stream().map(la -> LanguagePublicResponse.builder()
                                .language(la.getLanguage()).proficiency(la.getProficiency()).build()
                        )
                        .toList()

                )
                .consultationFees(interpreter.getConsultationFee() != null
                ? interpreter.getConsultationFee()
                        : BigDecimal.ZERO)
                .rating(interpreter.getRating())
                .ratingCount(interpreter.getRatingCount())

                .online(interpreter.isOnline())
                .available(interpreter.isAvailable())
                .lastSeenAt(interpreter.getLastSeenAt())
                .build();
    }
}