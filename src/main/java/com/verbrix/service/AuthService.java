package com.verbrix.service;

import com.verbrix.security.payload.response.SocialLoginRequest;
import com.verbrix.security.payload.request.*;
import com.verbrix.security.payload.response.LoginResponse;
import com.verbrix.security.payload.response.MessageResponse;
import com.verbrix.security.payload.response.OtpVerificationResponse;
import com.verbrix.security.payload.response.RefreshTokenResponse;
import org.springframework.transaction.annotation.Transactional;


public interface AuthService {

    MessageResponse requestRegistration(EmailRequest emailRequest);

    OtpVerificationResponse verifyOtp(OtpVerificationRequest request);

    LoginResponse completeRegistration(RegistrationRequest registrationRequest);

    LoginResponse loginUser(LoginRequest loginRequest);

    LoginResponse loginWithSocialProvider(SocialLoginRequest socialLoginRequest);

    RefreshTokenResponse refreshToken(RefreshTokenRequest request);

    MessageResponse requestPasswordReset(EmailRequest emailRequest);

    MessageResponse completePasswordReset(PasswordResetRequest passwordResetRequest);

    MessageResponse logoutUser(RefreshTokenRequest refreshRequest);


    @Transactional
    MessageResponse logoutFromAllDevices();
}
