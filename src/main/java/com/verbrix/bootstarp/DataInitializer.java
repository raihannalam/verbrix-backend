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

    // Realistic US first names
    private static final List<String> FIRST_NAMES = Arrays.asList(
            "James", "Mary", "Robert", "Patricia", "John", "Jennifer",
            "Michael", "Linda", "David", "Elizabeth", "William", "Barbara",
            "Richard", "Susan", "Joseph", "Jessica", "Thomas", "Sarah",
            "Christopher", "Karen", "Charles", "Lisa", "Daniel", "Nancy",
            "Matthew", "Sandra", "Anthony", "Ashley", "Mark", "Emily"
    );

    // Realistic US last names
    private static final List<String> LAST_NAMES = Arrays.asList(
            "Smith", "Johnson", "Williams", "Brown", "Jones", "Garcia",
            "Miller", "Davis", "Rodriguez", "Martinez", "Hernandez",
            "Lopez", "Gonzalez", "Wilson", "Anderson", "Thomas",
            "Taylor", "Moore", "Jackson", "Martin", "Lee", "Perez",
            "Thompson", "White"
    );

    private static final String DOMAIN = "@verbrix.raihanalam.dev";

    @Override
    @Transactional
    public void run(String... args) {

        log.info("Starting Production Data Initialization...");

        Role adminRole = getOrCreateRole(RoleType.ADMIN);
        Role interpreterRole = getOrCreateRole(RoleType.INTERPRETER);

        createAdmins(adminRole);
        createInterpreters(interpreterRole, 10);

        log.info("Production Data Initialization Completed.");
    }

    private Role getOrCreateRole(RoleType roleType) {
        return roleRepository.findByName(roleType)
                .orElseGet(() -> roleRepository.save(
                        Role.builder()
                                .name(roleType)
                                .build()
                ));
    }

    // =========================================================
    // ADMIN
    // =========================================================

    private void createAdmins(Role adminRole) {

        // Only Raihan admin
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

        if (userRepository.existsByEmail(email)) {
            log.info("Admin {} already exists. Skipping.", email);
            return;
        }

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

    private void createInterpreters(Role interpreterRole, int count) {

        List<MedicalSpecialization> allSpecs =
                Arrays.asList(MedicalSpecialization.values());

        List<Language> allLanguages =
                Arrays.asList(Language.values());

        List<GovernmentIdType> idTypes =
                Arrays.asList(GovernmentIdType.values());

        for (int i = 1; i <= count; i++) {

            // -------------------------------------------------
            // Generate the person's name FIRST
            // -------------------------------------------------

            String firstName =
                    FIRST_NAMES.get(random.nextInt(FIRST_NAMES.size()));

            String lastName =
                    LAST_NAMES.get(random.nextInt(LAST_NAMES.size()));

            // -------------------------------------------------
            // Convert name -> email
            //
            // Example:
            // James Smith
            // james.smith@verbrix.raihanalam.dev
            // -------------------------------------------------

            String email = generateUniqueEmail(firstName, lastName);

            // -------------------------------------------------
            // Create User
            // -------------------------------------------------

            User interpreterUser = User.builder()
                    .email(email)
                    .password(passwordEncoder.encode("Interpreter@123"))
                    .isActive(true)
                    .roles(Set.of(interpreterRole))
                    .build();

            interpreterUser = userRepository.save(interpreterUser);

            // -------------------------------------------------
            // Medical specializations
            // -------------------------------------------------

            Collections.shuffle(allSpecs);

            Set<MedicalSpecialization> specs =
                    allSpecs.stream()
                            .limit(2)
                            .collect(Collectors.toSet());

            MedicalSpecialization primarySpec =
                    specs.iterator().next();

            // -------------------------------------------------
            // Interpreter profile
            // -------------------------------------------------

            Interpreter interpreter = Interpreter.builder()
                    .firstName(firstName)
                    .lastName(lastName)

                    .bio(
                            String.format(
                                    "Professional interpreter with expertise in %s. " +
                                    "Committed to accurate medical communication.",
                                    primarySpec.getLabel()
                            )
                    )

                    .experienceYears(2 + random.nextInt(15))
                    .experienceMonths(random.nextInt(12))

                    .profilePictureUrl(
                            "https://randomuser.me/api/portraits/"
                                    + (random.nextBoolean() ? "men" : "women")
                                    + "/"
                                    + random.nextInt(99)
                                    + ".jpg"
                    )

                    .governmentIdType(
                            idTypes.get(random.nextInt(idTypes.size()))
                    )

                    .governmentIdDetails(
                            "US-ID-" + (30000 + i)
                    )

                    .governmentIdUrl(
                            "https://example.com/docs/id_" + i + ".jpg"
                    )

                    .introVideoUrl(
                            "https://www.youtube.com/watch?v=dQw4w9WgXcQ"
                    )

                    .status(VerificationStatus.VERIFIED)
                    .available(true)
                    .online(i % 2 == 0)
                    .lastSeenAt(Instant.now())

                    .ratingCount(random.nextInt(150))

                    .rating(
                            BigDecimal.valueOf(
                                    4.0 + (random.nextDouble() * 1.0)
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

            // -------------------------------------------------
            // Languages
            // -------------------------------------------------

            List<LanguageAbility> langs = new ArrayList<>();

            // English - C2
            langs.add(
                    LanguageAbility.builder()
                            .language(Language.EN)
                            .proficiency(ProficiencyLevel.C2)
                            .proofUrl(
                                    "https://certs.verbrix.com/en_native.pdf"
                            )
                            .build()
            );

            // Second language
            Language secondLang =
                    allLanguages.get(
                            random.nextInt(allLanguages.size())
                    );

            if (secondLang != Language.EN) {

                langs.add(
                        LanguageAbility.builder()
                                .language(secondLang)
                                .proficiency(
                                        ProficiencyLevel.values()[
                                                random.nextInt(
                                                        ProficiencyLevel.values().length
                                                )
                                        ]
                                )
                                .proofUrl(
                                        "https://certs.verbrix.com/lang_cert_"
                                                + i
                                                + ".pdf"
                                )
                                .build()
                );
            }

            interpreter.setLanguageAbilities(langs);

            interpreter =
                    interpreterRepository.save(interpreter);

            // -------------------------------------------------
            // Certification
            // -------------------------------------------------

            certificationRepository.save(
                    Certification.builder()
                            .name(
                                    "National Board Certified Medical Interpreter"
                            )
                            .issuingOrganization("NBCMI")
                            .fileUrl(
                                    "https://example.com/cert/cmi_"
                                            + i
                                            + ".pdf"
                            )
                            .description(
                                    "Standard certification for US healthcare environments."
                            )
                            .issueDate(
                                    LocalDate.now().minusYears(2)
                            )
                            .expiryDate(
                                    LocalDate.now().plusYears(1)
                            )
                            .status(VerificationStatus.VERIFIED)
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
    }

    // =========================================================
    // EMAIL GENERATOR
    // =========================================================

    private String generateUniqueEmail(
            String firstName,
            String lastName
    ) {

        String baseEmail =
                firstName.toLowerCase(Locale.ROOT)
                        + "."
                        + lastName.toLowerCase(Locale.ROOT)
                        + DOMAIN;

        // First person with this name
        if (!userRepository.existsByEmail(baseEmail)) {
            return baseEmail;
        }

        // Same name already exists
        int suffix = 2;

        while (
                userRepository.existsByEmail(
                        firstName.toLowerCase(Locale.ROOT)
                                + "."
                                + lastName.toLowerCase(Locale.ROOT)
                                + suffix
                                + DOMAIN
                )
        ) {
            suffix++;
        }

        return firstName.toLowerCase(Locale.ROOT)
                + "."
                + lastName.toLowerCase(Locale.ROOT)
                + suffix
                + DOMAIN;
    }
}
