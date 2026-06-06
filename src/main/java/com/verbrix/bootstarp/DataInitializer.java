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
import org.springframework.context.annotation.Profile;
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

    // Pool for Realistic US Names
    private static final List<String> FIRST_NAMES = Arrays.asList(
            "James", "Mary", "Robert", "Patricia", "John", "Jennifer", "Michael", "Linda",
            "David", "Elizabeth", "William", "Barbara", "Richard", "Susan", "Joseph", "Jessica",
            "Thomas", "Sarah", "Christopher", "Karen", "Charles", "Lisa", "Daniel", "Nancy",
            "Matthew", "Sandra", "Anthony", "Ashley", "Mark", "Emily"
    );

    private static final List<String> LAST_NAMES = Arrays.asList(
            "Smith", "Johnson", "Williams", "Brown", "Jones", "Garcia", "Miller", "Davis",
            "Rodriguez", "Martinez", "Hernandez", "Lopez", "Gonzalez", "Wilson", "Anderson",
            "Thomas", "Taylor", "Moore", "Jackson", "Martin", "Lee", "Perez", "Thompson", "White"
    );

    @Override
    @Transactional
    public void run(String... args) {
        log.info("Starting Production Data Initialization with US Personas...");

        Role adminRole = getOrCreateRole(RoleType.ADMIN);
        Role interpreterRole = getOrCreateRole(RoleType.INTERPRETER);

        createAdmins(adminRole);
        createInterpreters(interpreterRole, 10);

        log.info("Production Data Initialization Completed.");
    }

    private Role getOrCreateRole(RoleType roleType) {
        return roleRepository.findByName(roleType)
                .orElseGet(() -> roleRepository.save(
                        Role.builder().name(roleType).build()
                ));
    }

    private void createAdmins(Role adminRole) {
        createAdminIfNotExists("nancyadmin@verbrix.com", "nancy@9900", adminRole);
        createAdminIfNotExists("raihanadmin@verbrix.com", "Raihan@2003!!!!", adminRole);
    }

    private void createAdminIfNotExists(String email, String rawPassword, Role role) {
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

    private void createInterpreters(Role interpreterRole, int count) {
        List<MedicalSpecialization> allSpecs = Arrays.asList(MedicalSpecialization.values());
        List<Language> allLanguages = Arrays.asList(Language.values());
        List<GovernmentIdType> idTypes = Arrays.asList(GovernmentIdType.values());

        for (int i = 1; i <= count; i++) {
            // Using a distinct suffix to differentiate from original placeholder data if needed
            String email = "interpreterp" + i + "@verbrix.com";

            if (userRepository.existsByEmail(email)) {
                continue;
            }

            // Generate Realistic US Name
            String firstName = FIRST_NAMES.get(random.nextInt(FIRST_NAMES.size()));
            String lastName = LAST_NAMES.get(random.nextInt(LAST_NAMES.size()));

            User interpreterUser = User.builder()
                    .email(email)
                    .password(passwordEncoder.encode("Interpreter@123"))
                    .isActive(true)
                    .roles(Set.of(interpreterRole))
                    .build();

            interpreterUser = userRepository.save(interpreterUser);

            // Select 2 unique random specializations
            Collections.shuffle(allSpecs);
            Set<MedicalSpecialization> specs = allSpecs.stream().limit(2).collect(Collectors.toSet());
            MedicalSpecialization primarySpec = specs.iterator().next();

            Interpreter interpreter = Interpreter.builder()
                    .firstName(firstName)
                    .lastName(lastName)
                    .bio(String.format("Professional interpreter with expertise in %s. Committed to accurate medical communication.", primarySpec.getLabel()))
                    .experienceYears(2 + random.nextInt(15))
                    .experienceMonths(random.nextInt(12))
                    .profilePictureUrl("https://randomuser.me/api/portraits/" + (random.nextBoolean() ? "men" : "women") + "/" + random.nextInt(99) + ".jpg")
                    .governmentIdType(idTypes.get(random.nextInt(idTypes.size())))
                    .governmentIdDetails("US-ID-" + (30000 + i))
                    .governmentIdUrl("https://example.com/docs/id_" + i + ".jpg")
                    .introVideoUrl("https://www.youtube.com/watch?v=dQw4w9WgXcQ")
                    .status(VerificationStatus.VERIFIED)
                    .available(true)
                    .online(i % 2 == 0)
                    .lastSeenAt(Instant.now())
                    .ratingCount(random.nextInt(150))
                    .rating(BigDecimal.valueOf(4.0 + (random.nextDouble() * 1.0)))
                    .consultationFee(BigDecimal.valueOf(30 + random.nextInt(40)))
                    .serviceAgreementFee(BigDecimal.valueOf(150))
                    .recurringFeeAmount(BigDecimal.valueOf(49.99))
                    .recurringFeeFrequency(RecurringFeeFrequency.MONTHLY)
                    .user(interpreterUser)
                    .build();

            interpreter.setSpecializations(specs);

            // Add English (Tier 1) and one random Tier 2 or Indian Regional language
            List<LanguageAbility> langs = new ArrayList<>();
            langs.add(LanguageAbility.builder()
                    .language(Language.EN)
                    .proficiency(ProficiencyLevel.C2)
                    .proofUrl("https://certs.verbrix.com/en_native.pdf")
                    .build());

            Language secondLang = allLanguages.get(random.nextInt(allLanguages.size()));
            if (secondLang != Language.EN) {
                langs.add(LanguageAbility.builder()
                        .language(secondLang)
                        .proficiency(ProficiencyLevel.values()[random.nextInt(ProficiencyLevel.values().length)])
                        .proofUrl("https://certs.verbrix.com/lang_cert_" + i + ".pdf")
                        .build());
            }

            interpreter.setLanguageAbilities(langs);
            interpreter = interpreterRepository.save(interpreter);

            certificationRepository.save(Certification.builder()
                    .name("National Board Certified Medical Interpreter")
                    .issuingOrganization("NBCMI")
                    .fileUrl("https://example.com/cert/cmi_" + i + ".pdf")
                    .description("Standard certification for US healthcare environments.")
                    .issueDate(LocalDate.now().minusYears(2))
                    .expiryDate(LocalDate.now().plusYears(1))
                    .status(VerificationStatus.VERIFIED)
                    .interpreter(interpreter)
                    .build());

            log.info("Created US-Based Interpreter: {} {} ({})", firstName, lastName, email);
        }
    }
}