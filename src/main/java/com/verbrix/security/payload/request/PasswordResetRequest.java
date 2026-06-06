package com.verbrix.security.payload.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class PasswordResetRequest {

    // --- REMOVED 'email' FIELD ---

    @NotBlank
    @Size(min = 6, max = 40, message = "Password must be between 6 and 40 characters")
    private String newPassword;

    @NotBlank
    private String preAuthToken; // --- NEW ---
}