package com.verbrix.model.requirement;

import com.verbrix.model.enums.MedicalSpecialization;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class Specializations {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING) // This tells JPA to store "CARDIOLOGY" instead of 1
    @Column(unique = true, nullable = false)
    private MedicalSpecialization name;
}