package com.verbrix.payload.publiccontroller.response;

import com.verbrix.model.enums.Language;
import com.verbrix.model.enums.ProficiencyLevel;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LanguagePublicResponse {

    private Language language;
    private ProficiencyLevel proficiency;
}
