package com.verbrix.security.payload.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequest {

    @NotBlank
    @Email
    private String email;

    @NotBlank
    private String password;

//    @NotBlank(message = "Device ID is required for security tracking")
    private String deviceId;

    private String deviceDetails;
}
