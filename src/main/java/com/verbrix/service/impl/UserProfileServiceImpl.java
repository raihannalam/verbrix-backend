package com.verbrix.service.impl;

import com.verbrix.payload.profile.PasswordUpdateRequest;
import com.verbrix.payload.profile.UserProfileResponse;
import com.verbrix.payload.profile.UserProfileUpdateRequest;
import com.verbrix.model.profile.Client;
import com.verbrix.model.profile.Interpreter;
import com.verbrix.model.rbac.User;
import com.verbrix.repository.ClientRepository;
import com.verbrix.repository.InterpreterRepository;
import com.verbrix.repository.UserRepository;
import com.verbrix.service.UserProfileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserProfileServiceImpl implements UserProfileService {

    private final UserRepository userRepository;
    private final ClientRepository clientRepository;
    private final InterpreterRepository interpreterRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    @Override
    public UserProfileResponse getProfile(String email) {
        User user = getUserByEmail(email);
        return mapToResponse(user);
    }

    @Transactional
    @Override
    public UserProfileResponse updateProfile(String email, UserProfileUpdateRequest request) {
        User user = getUserByEmail(email);

        // 1. Update Core User Data
        if (request.phone() != null && !request.phone().isBlank()) {
            user.setPhone(request.phone());
            userRepository.save(user);
        }

        // 2. Update Client Profile (if exists)
        Client client = user.getClientProfile();
        if (client != null) {
            client.setFirstName(request.firstName());
            client.setLastName(request.lastName());
            if (request.profilePictureUrl() != null) {
                client.setProfilePictureUrl(request.profilePictureUrl());
            }
            clientRepository.save(client);
        }

        // 3. Update Interpreter Profile (if exists)
        Interpreter interpreter = user.getInterpreterProfile();
        if (interpreter != null) {
            interpreter.setFirstName(request.firstName());
            interpreter.setLastName(request.lastName());
            if (request.profilePictureUrl() != null) {
                interpreter.setProfilePictureUrl(request.profilePictureUrl());
            }
            interpreterRepository.save(interpreter);
        }

        // Refresh the user entity to ensure we return the latest state
        return mapToResponse(user);
    }

    @Transactional
    @Override
    public void updatePassword(String email, PasswordUpdateRequest request) {
        User user = getUserByEmail(email);

        // Verify current password
        if (!passwordEncoder.matches(request.currentPassword(), user.getPassword())) {
            log.warn("Failed password update attempt for user: {}", email);
            // TODO: Replace with your custom production exception (e.g., BadCredentialsException)
            throw new IllegalArgumentException("Incorrect current password.");
        }

        // Check if new password is the same as the old one (optional but good practice)
        if (passwordEncoder.matches(request.newPassword(), user.getPassword())) {
            throw new IllegalArgumentException("New password cannot be the same as the current password.");
        }

        // Encode and save new password
        user.setPassword(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);

        log.info("Password successfully updated for user: {}", email);
    }

    // --- Helper Methods ---

    private User getUserByEmail(String email) {
        // TODO: Replace RuntimeException with your specific ResourceNotFoundException
        return userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.error("User not found with email: {}", email);
                    return new RuntimeException("User not found.");
                });
    }

    private UserProfileResponse mapToResponse(User user) {
        String firstName = null;
        String lastName = null;
        String profilePicUrl = null;

        // An Interpreter profile takes precedence for fetching display info
        if (user.getInterpreterProfile() != null) {
            firstName = user.getInterpreterProfile().getFirstName();
            lastName = user.getInterpreterProfile().getLastName();
            profilePicUrl = user.getInterpreterProfile().getProfilePictureUrl();
        } else if (user.getClientProfile() != null) {
            firstName = user.getClientProfile().getFirstName();
            lastName = user.getClientProfile().getLastName();
            profilePicUrl = user.getClientProfile().getProfilePictureUrl();
        }

        return new UserProfileResponse(
                firstName,
                lastName,
                user.getEmail(),
                user.getPhone(),
                profilePicUrl
        );
    }
}