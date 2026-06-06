// src/main/java/com/verbrix/security/payload/request/OtpVerificationRequest.java
package com.verbrix.security.payload.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OtpVerificationRequest {
    private String email;
    private String otp;
}
