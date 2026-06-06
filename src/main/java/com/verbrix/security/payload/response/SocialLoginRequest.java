package com.verbrix.security.payload.response;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SocialLoginRequest {

    @NotBlank
    private String idToken;

//    @NotBlank(message = "Device ID is required for security tracking")
    private String deviceId;

    private String deviceDetails;
}