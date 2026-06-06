package com.verbrix.payload.interpretercontroller.request;

import com.verbrix.model.enums.Language;
import com.verbrix.model.enums.ProficiencyLevel;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LanguageAbilityRequest {

    @NotNull(message = "Language selection is required")
    private Language language;

    @NotNull(message = "Proficiency level is required")
    private ProficiencyLevel proficiency;

    private String proofUrl;
}