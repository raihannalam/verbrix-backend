package com.verbrix.security.payload.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegistrationRequest {

    @NotBlank
    @Email
    private String email; // Kept for cross-checking the token

    @NotBlank
    @Size(min = 6, max = 40)
    private String password;

    @NotBlank
    private String firstName;

    @NotBlank
    private String lastName;

    @NotBlank
    private String preAuthToken; // --- NEW ---

    //    @NotBlank(message = "Device ID is required for security tracking")
    private String deviceId;

    private String deviceDetails;

}