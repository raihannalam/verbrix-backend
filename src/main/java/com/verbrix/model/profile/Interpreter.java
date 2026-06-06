package com.verbrix.model.profile;

import com.verbrix.model.enums.GovernmentIdType;
import com.verbrix.model.enums.RecurringFeeFrequency;
import com.verbrix.model.enums.VerificationStatus;
import com.verbrix.model.enums.MedicalSpecialization;
import com.verbrix.model.rbac.User;
import com.verbrix.model.requirement.Certification;
import com.verbrix.model.requirement.LanguageAbility;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString(exclude = {"certifications", "languageAbilities", "user"}) // Exclude User too to prevent cycles
@Builder
public class Interpreter {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // --- Personal Info ---
    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    @Column(columnDefinition = "TEXT")
    private String bio;

    @Column(name = "experience_years")
    private Integer experienceYears;

    @Column(name = "experience_months")
    private Integer experienceMonths;
    // --- URLs & Documents ---
    @Column(name = "profile_picture_url")
    private String profilePictureUrl;

    @Enumerated(EnumType.STRING)
    private GovernmentIdType governmentIdType;

    private String governmentIdDetails;

    @Column(name = "government_id_url", nullable = false)
    private String governmentIdUrl;

    @Column(name = "intro_video_url")
    private String introVideoUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VerificationStatus status;

    @Column(nullable = false)
    private boolean available = false;

    @Column(name = "rejection_reason", columnDefinition = "TEXT")
    private String rejectionReason;


    private boolean online;

    @Column(name = "last_seen_at")
    private Instant lastSeenAt;

    // --- Ratings ---
    // CHANGED: String -> Integer (Critical for math)
    @Column(name = "rating_count")
    private Integer ratingCount = 0;

    @Column(precision = 10, scale = 2)
    private BigDecimal rating;

    // --- Financials ---
    @Column(precision = 10, scale = 2)
    private BigDecimal consultationFee;

    @Column(precision = 10, scale = 2)
    private BigDecimal serviceAgreementFee;

    @Column(precision = 10, scale = 2)
    private BigDecimal recurringFeeAmount;

    @Enumerated(EnumType.STRING)
    private RecurringFeeFrequency recurringFeeFrequency;

    // --- Relationships ---
    @OneToOne(fetch = FetchType.EAGER) // Lazy is better for performance here
    @JoinColumn(name = "user_id", referencedColumnName = "id", unique = true, nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "last_modified_by")
    private User lastModifiedBy;

    // --- Collections ---
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "interpreter_language_abilities",
            joinColumns = @JoinColumn(name = "interpreter_id"))
    private List<LanguageAbility> languageAbilities = new ArrayList<>();

    @ElementCollection(targetClass = MedicalSpecialization.class, fetch = FetchType.EAGER)
    @CollectionTable(name = "interpreter_specializations",
            joinColumns = @JoinColumn(name = "interpreter_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "specialization")
    private Set<MedicalSpecialization> specializations = new HashSet<>();

    @OneToMany(mappedBy = "interpreter", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Certification> certifications;


    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "last_modified")
    private LocalDateTime lastModified;
}