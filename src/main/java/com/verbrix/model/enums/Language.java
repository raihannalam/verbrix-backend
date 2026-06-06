package com.verbrix.model.enums;

import lombok.Getter;

@Getter
public enum Language {
    // Global Tier 1
    EN("English"),
    ES("Spanish"),
    FR("French"),
    AR("Arabic"),
    ZH("Chinese (Mandarin)"),
    DE("German"),
    PT("Portuguese"),

    // Tier 2 & Regional
    RU("Russian"),
    JA("Japanese"),
    KO("Korean"),
    TR("Turkish"),
    VI("Vietnamese"),
    IT("Italian"),
    NL("Dutch"),
    PL("Polish"),

    // Indian Regional (Crucial for Verbrix)
    HI("Hindi"),
    BN("Bengali"),
    TE("Telugu"),
    MR("Marathi"),
    TA("Tamil"),
    UR("Urdu"),
    GU("Gujarati"),
    KN("Kannada"),
    ML("Malayalam"),
    PA("Punjabi"),
    AS("Assamese"),

    // Special
    ASL("American Sign Languages"),
    ISL("Indian Sign Languages");

    private final String label;
    Language(String label) { this.label = label; }
}