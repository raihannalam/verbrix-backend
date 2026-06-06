package com.verbrix.payload.admininterpretercontroller.response;

import com.verbrix.model.enums.Language;
import com.verbrix.model.enums.ProficiencyLevel;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@Builder
@AllArgsConstructor
public class LanguageAbilityAdminResponse {
    @NotBlank
    Language language;

    @NotBlank
    ProficiencyLevel proficiency;
}
