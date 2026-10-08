package com.verbrix.bootstarp;

import com.verbrix.model.enums.*;
import com.verbrix.model.profile.Interpreter;
import com.verbrix.model.rbac.Role;
import com.verbrix.model.rbac.User;
import com.verbrix.model.requirement.Certification;
import com.verbrix.model.requirement.LanguageAbility;
import com.verbrix.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final InterpreterRepository interpreterRepository;
    private final CertificationRepository certificationRepository;
    private final PasswordEncoder passwordEncoder;

    private final Random random = new Random();

    // =========================================================
    // DATA
    // =========================================================

    private static final List<String> FIRST_NAMES = Arrays.asList(
            "James", "Mary", "Robert", "Patricia", "John", "Jennifer",
            "Michael", "Linda", "David", "Elizabeth", "William", "Barbara",
            "Richard", "Susan", "Joseph", "Jessica", "Thomas", "Sarah",
            "Christopher", "Karen", "Charles", "Lisa", "Daniel", "Nancy",
            "Matthew", "Sandra", "Anthony", "Ashley", "Mark", "Emily"
    );

    private static final List<String> LAST_NAMES = Arrays.asList(
            "Smith", "Johnson", "Williams", "Brown", "Jones", "Garcia",
            "Miller", "Davis", "Rodriguez", "Martinez", "Hernandez",
            "Lopez", "Gonzalez", "Wilson", "Anderson", "Thomas",
            "Taylor", "Moore", "Jackson", "Martin", "Lee", "Perez",
            "Thompson", "White"
    );

    private static final String DOMAIN = "@verbrix.raihanalam.dev";

    private static final int REQUIRED_INTERPRETERS = 10;

    // =========================================================
    // STARTUP
    // =========================================================

    @Override
    @Transactional
    public void run(String... args) {

        log.info("Starting Production Data Initialization...");

        // -----------------------------------------------------
        // ROLES
        // -----------------------------------------------------

        Role adminRole = getOrCreateRole(RoleType.ADMIN);
        Role interpreterRole = getOrCreateRole(RoleType.INTERPRETER);

        // -----------------------------------------------------
        // ADMIN
        // -----------------------------------------------------

        createAdmins(adminRole);

        // -----------------------------------------------------
        // INTERPRETERS
        // -----------------------------------------------------

        createInterpreters(interpreterRole, REQUIRED_INTERPRETERS);

        log.info("Production Data Initialization Completed.");
    }

    // =========================================================
    // ROLES
    // =========================================================

    private Role getOrCreateRole(RoleType roleType) {

        return roleRepository.findByName(roleType)
                .orElseGet(() -> {

                    log.info("Creating missing role: {}", roleType);

                    return roleRepository.save(
                            Role.builder()
                                    .name(roleType)
                                    .build()
                    );
                });
    }

    // =========================================================
    // ADMIN
    // =========================================================

    private void createAdmins(Role adminRole) {

        createAdminIfNotExists(
                "raihanadmin@verbrix.raihanalam.dev",
                "Raihan@2003!!!!",
                adminRole
        );
    }

    private void createAdminIfNotExists(
            String email,
            String rawPassword,
            Role role
    ) {

        // -----------------------------------------------------
        // DO NOT CREATE IF ALREADY EXISTS
        // -----------------------------------------------------

        if (userRepository.existsByEmail(email)) {

            log.info(
                    "Admin already exists: {}. Skipping.",
                    email
            );

            return;
        }

        // -----------------------------------------------------
        // CREATE ADMIN
        // -----------------------------------------------------

        User admin = User.builder()
                .email(email)
                .password(passwordEncoder.encode(rawPassword))
                .isActive(true)
                .roles(Set.of(role))
                .build();

        userRepository.save(admin);

        log.info("Admin created: {}", email);
    }

    // =========================================================
    // INTERPRETERS
    // =========================================================

    private void createInterpreters(
            Role interpreterRole,
            int requiredCount
    ) {

        // -----------------------------------------------------
        // CHECK HOW MANY INTERPRETERS ALREADY EXIST
        // -----------------------------------------------------

        long existingCount = interpreterRepository.count();

        log.info(
                "Existing interpreters: {} / {}",
                existingCount,
                requiredCount
        );

        // -----------------------------------------------------
        // EVERYTHING ALREADY EXISTS
        // -----------------------------------------------------

        if (existingCount >= requiredCount) {

            log.info(
                    "Required number of interpreters already exists. Skipping creation."
            );

            return;
        }

        // -----------------------------------------------------
        // ONLY CREATE THE MISSING ONES
        // -----------------------------------------------------

        int missingCount =
                requiredCount - (int) existingCount;

        log.info(
                "Creating {} missing interpreter(s).",
                missingCount
        );

        List<MedicalSpecialization> allSpecs =
                Arrays.asList(MedicalSpecialization.values());

        List<Language> allLanguages =
                Arrays.asList(Language.values());

        List<GovernmentIdType> idTypes =
                Arrays.asList(GovernmentIdType.values());

        for (int i = 1; i <= missingCount; i++) {

            createSingleInterpreter(
                    interpreterRole,
                    allSpecs,
                    allLanguages,
                    idTypes
            );
        }
    }

    // =========================================================
    // CREATE SINGLE INTERPRETER
    // =========================================================

    private void createSingleInterpreter(
            Role interpreterRole,
            List<MedicalSpecialization> allSpecs,
            List<Language> allLanguages,
            List<GovernmentIdType> idTypes
    ) {

        // -----------------------------------------------------
        // GENERATE NAME
        // -----------------------------------------------------

        String firstName =
                FIRST_NAMES.get(
                        random.nextInt(FIRST_NAMES.size())
                );

        String lastName =
                LAST_NAMES.get(
                        random.nextInt(LAST_NAMES.size())
                );

        // -----------------------------------------------------
        // GENERATE UNIQUE EMAIL
        // -----------------------------------------------------

        String email =
                generateUniqueEmail(
                        firstName,
                        lastName
                );

        // -----------------------------------------------------
        // USER
        // -----------------------------------------------------

        User interpreterUser = User.builder()
                .email(email)
                .password(
                        passwordEncoder.encode(
                                "Interpreter@123"
                        )
                )
                .isActive(true)
                .roles(Set.of(interpreterRole))
                .build();

        interpreterUser =
                userRepository.save(interpreterUser);

        // -----------------------------------------------------
        // MEDICAL SPECIALIZATIONS
        // -----------------------------------------------------

        Collections.shuffle(allSpecs);

        Set<MedicalSpecialization> specs =
                allSpecs.stream()
                        .limit(2)
                        .collect(Collectors.toSet());

        MedicalSpecialization primarySpec =
                specs.iterator().next();

        // -----------------------------------------------------
        // INTERPRETER PROFILE
        // -----------------------------------------------------

        Interpreter interpreter = Interpreter.builder()

                .firstName(firstName)

                .lastName(lastName)

                .bio(
                        String.format(
                                "Professional interpreter with expertise in %s. "
                                        + "Committed to accurate medical communication.",
                                primarySpec.getLabel()
                        )
                )

                .experienceYears(
                        2 + random.nextInt(15)
                )

                .experienceMonths(
                        random.nextInt(12)
                )

                .profilePictureUrl(
                        "https://randomuser.me/api/portraits/"
                                + (random.nextBoolean()
                                ? "men"
                                : "women")
                                + "/"
                                + random.nextInt(99)
                                + ".jpg"
                )

                .governmentIdType(
                        idTypes.get(
                                random.nextInt(
                                        idTypes.size()
                                )
                        )
                )

                .governmentIdDetails(
                        "US-ID-"
                                + UUID.randomUUID()
                )

                .governmentIdUrl(
                        "https://example.com/docs/id_"
                                + UUID.randomUUID()
                                + ".jpg"
                )

                .introVideoUrl(
                        "https://www.youtube.com/watch?v=dQw4w9WgXcQ"
                )

                .status(
                        VerificationStatus.VERIFIED
                )

                .available(true)

                .online(false)

                .lastSeenAt(
                        Instant.now()
                )

                .ratingCount(
                        random.nextInt(150)
                )

                .rating(
                        BigDecimal.valueOf(
                                4.0
                                        + (
                                        random.nextDouble()
                                                * 1.0
                                )
                        )
                )

                .consultationFee(
                        BigDecimal.valueOf(
                                30 + random.nextInt(40)
                        )
                )

                .serviceAgreementFee(
                        BigDecimal.valueOf(150)
                )

                .recurringFeeAmount(
                        BigDecimal.valueOf(49.99)
                )

                .recurringFeeFrequency(
                        RecurringFeeFrequency.MONTHLY
                )

                .user(interpreterUser)

                .build();

        interpreter.setSpecializations(specs);

        // -----------------------------------------------------
        // LANGUAGES
        // -----------------------------------------------------

        List<LanguageAbility> langs =
                new ArrayList<>();

        // English - C2

        langs.add(
                LanguageAbility.builder()
                        .language(Language.EN)
                        .proficiency(
                                ProficiencyLevel.C2
                        )
                        .proofUrl(
                                "https://certs.verbrix.com/en_native.pdf"
                        )
                        .build()
        );

        // -----------------------------------------------------
        // SECOND LANGUAGE
        // -----------------------------------------------------

        Language secondLang =
                allLanguages.get(
                        random.nextInt(
                                allLanguages.size()
                        )
                );

        if (secondLang != Language.EN) {

            langs.add(
                    LanguageAbility.builder()
                            .language(secondLang)
                            .proficiency(
                                    ProficiencyLevel.values()[
                                            random.nextInt(
                                                    ProficiencyLevel
                                                            .values()
                                                            .length
                                            )
                                            ]
                            )
                            .proofUrl(
                                    "https://certs.verbrix.com/lang_cert_"
                                            + UUID.randomUUID()
                                            + ".pdf"
                            )
                            .build()
            );
        }

        interpreter.setLanguageAbilities(langs);

        // -----------------------------------------------------
        // SAVE INTERPRETER
        // -----------------------------------------------------

        interpreter =
                interpreterRepository.save(
                        interpreter
                );

        // -----------------------------------------------------
        // CERTIFICATION
        // -----------------------------------------------------

        certificationRepository.save(
                Certification.builder()
                        .name(
                                "National Board Certified Medical Interpreter"
                        )
                        .issuingOrganization("NBCMI")
                        .fileUrl(
                                "https://example.com/cert/cmi_"
                                        + UUID.randomUUID()
                                        + ".pdf"
                        )
                        .description(
                                "Standard certification for US healthcare environments."
                        )
                        .issueDate(
                                LocalDate.now()
                                        .minusYears(2)
                        )
                        .expiryDate(
                                LocalDate.now()
                                        .plusYears(1)
                        )
                        .status(
                                VerificationStatus.VERIFIED
                        )
                        .interpreter(interpreter)
                        .build()
        );

        log.info(
                "Created Interpreter: {} {} -> {}",
                firstName,
                lastName,
                email
        );
    }

    // =========================================================
    // EMAIL GENERATOR
    // =========================================================

    private String generateUniqueEmail(
            String firstName,
            String lastName
    ) {

        String first =
                firstName.toLowerCase(Locale.ROOT);

        String last =
                lastName.toLowerCase(Locale.ROOT);

        String baseEmail =
                first
                        + "."
                        + last
                        + DOMAIN;

        // -----------------------------------------------------
        // FIRST POSSIBILITY
        // -----------------------------------------------------

        if (!userRepository.existsByEmail(baseEmail)) {
            return baseEmail;
        }

        // -----------------------------------------------------
        // TRY suffixes
        // -----------------------------------------------------

        int suffix = 2;

        while (true) {

            String email =
                    first
                            + "."
                            + last
                            + suffix
                            + DOMAIN;

            if (!userRepository.existsByEmail(email)) {
                return email;
            }

            suffix++;
        }
    }
}