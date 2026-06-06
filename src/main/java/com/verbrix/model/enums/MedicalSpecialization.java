package com.verbrix.model.enums;

import lombok.Getter;

@Getter
public enum MedicalSpecialization {
    // Core Clinical Specialties
    GENERAL_PRACTICE("General Practice & Family Medicine"),
    CARDIOLOGY("Cardiology & Vascular Medicine"),
    ONCOLOGY("Oncology & Cancer Care"),
    PEDIATRICS("Pediatrics & Neonatal Care"),
    NEUROLOGY("Neurology & Neurosurgery"),
    ORTHOPEDICS("Orthopedics & Sports Medicine"),
    OBSTETRICS_GYNECOLOGY("Obstetrics & Gynecology (OB/GYN)"),
    PSYCHIATRY("Psychiatry & Behavioral Health"),
    DERMATOLOGY("Dermatology"),
    GASTROENTEROLOGY("Gastroenterology"),
    EMERGENCY_MEDICINE("Emergency & Trauma Care"),
    SURGERY("General & Specialized Surgery"),
    IMMUNOLOGY("Allergy & Immunology"),
    ENDOCRINOLOGY("Endocrinology & Diabetes"),
    UROLOGY("Urology"),
    NEPHROLOGY("Nephrology (Kidney Care)"),
    OPHTHALMOLOGY("Ophthalmology (Eye Care)"),
    OTOLARYNGOLOGY("Otolaryngology (ENT)"),
    RHEUMATOLOGY("Rheumatology & Arthritis"),

    // High-Demand 2026 Trends
    GERIATRICS("Geriatric Medicine (Elderly Care)"),
    PALLIATIVE_CARE("Hospice & Palliative Care"),
    REHABILITATION("Physical Medicine & Rehabilitation"),
    RADIOLOGY("Radiology & Diagnostic Imaging"),
    GENETICS("Medical Genetics & Precision Medicine"),
    INFECTIOUS_DISEASES("Infectious Diseases & Epidemiology"),
    DENTISTRY("Dentistry & Oral Health"),

    // Industrial & Regulatory (Huge for Translation Revenue)
    CLINICAL_TRIALS("Clinical Research & Trials"),
    PHARMACEUTICALS("Pharmaceutical & Life Sciences"),
    MEDICAL_DEVICES("Medical Devices & Regulatory Affairs"),
    TELEHEALTH("Digital Health & Telemedicine");

    private final String label;

    MedicalSpecialization(String label) {
        this.label = label;
    }
}