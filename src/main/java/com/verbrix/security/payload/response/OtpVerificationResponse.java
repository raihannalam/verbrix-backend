package com.verbrix.security.payload.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class OtpVerificationResponse {
    private String message;
    private String preAuthToken; // The "hall pass" token
}