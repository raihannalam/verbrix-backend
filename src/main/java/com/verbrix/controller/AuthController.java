package com.verbrix.controller;

import com.verbrix.security.jwt.CookieUtils;
import com.verbrix.security.payload.response.SocialLoginRequest;
import com.verbrix.security.payload.request.*;
import com.verbrix.security.payload.response.LoginResponse;
import com.verbrix.security.payload.response.MessageResponse;
import com.verbrix.security.payload.response.RefreshTokenResponse;
import com.verbrix.security.payload.response.OtpVerificationResponse;
import com.verbrix.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;
    private final CookieUtils cookieUtils;

    @PostMapping("/register/request-otp")
    public ResponseEntity<MessageResponse> requestRegistrationOtp(
            @Valid @RequestBody EmailRequest emailRequest) {
        MessageResponse response = authService.requestRegistration(emailRequest);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/register/complete")
    public ResponseEntity<LoginResponse> completeRegistration(
            @Valid @RequestBody RegistrationRequest registrationRequest) {
        LoginResponse response = authService.completeRegistration(registrationRequest);
        ResponseCookie cookie = cookieUtils.generateRefreshCookie(response.getRefreshToken());

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(response);
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<OtpVerificationResponse> verifyOtp(
            @Valid @RequestBody OtpVerificationRequest request) {
        OtpVerificationResponse response = authService.verifyOtp(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody LoginRequest loginRequest) {
        LoginResponse response = authService.loginUser(loginRequest);
        ResponseCookie cookie = cookieUtils.generateRefreshCookie(response.getRefreshToken());

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(response);
    }

    @PostMapping("/social-login")
    public ResponseEntity<LoginResponse> socialLogin(
            @Valid @RequestBody SocialLoginRequest socialLoginRequest) {
        LoginResponse response = authService.loginWithSocialProvider(socialLoginRequest);
        ResponseCookie cookie = cookieUtils.generateRefreshCookie(response.getRefreshToken());

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(response);
    }

    @PostMapping("/password/request-reset")
    public ResponseEntity<MessageResponse> requestPasswordReset(
            @Valid @RequestBody EmailRequest emailRequest) {
        MessageResponse response = authService.requestPasswordReset(emailRequest);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/password/complete-reset")
    public ResponseEntity<MessageResponse> completePasswordReset(
            @Valid @RequestBody PasswordResetRequest request) {
        MessageResponse response = authService.completePasswordReset(request);
        return ResponseEntity.ok(response);
    }

    // 🟢 DUAL SUPPORT: Angular (Cookie) & Android (JSON Body)
    @PostMapping("/refresh-token")
    public ResponseEntity<RefreshTokenResponse> refreshToken(
            @CookieValue(name = "verbrix_refresh", required = false) String refreshTokenCookie,
            // 🟢 REMOVED @Valid so an empty body from Angular doesn't trigger a 400 Bad Request
            @RequestBody(required = false) RefreshTokenRequest request) {

        log.debug("Refresh Attempt - Cookie Present: {}", (refreshTokenCookie != null));

        String tokenToUse = (refreshTokenCookie != null && !refreshTokenCookie.isBlank())
                ? refreshTokenCookie
                : (request != null ? request.getRefreshToken() : null);

        if (tokenToUse == null || tokenToUse.isBlank()) {
            log.warn("Refresh failed: No token found in cookie or request body");
            return ResponseEntity.badRequest().build();
        }

        RefreshTokenResponse response = authService.refreshToken(new RefreshTokenRequest(tokenToUse));
        ResponseCookie newCookie = cookieUtils.generateRefreshCookie(response.getRefreshToken());

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, newCookie.toString())
                .body(response);
    }

    // 🟢 DUAL SUPPORT: Angular (Cookie) & Android (JSON Body)
    @PostMapping("/logout")
    public ResponseEntity<MessageResponse> logoutUser(
            @CookieValue(name = "verbrix_refresh", required = false) String refreshTokenCookie,
            // 🟢 REMOVED @Valid
            @RequestBody(required = false) RefreshTokenRequest refreshRequest) {

        String tokenToUse = (refreshTokenCookie != null && !refreshTokenCookie.isBlank())
                ? refreshTokenCookie
                : (refreshRequest != null ? refreshRequest.getRefreshToken() : null);

        // Try to invalidate the token in the database, but don't crash if it fails
        if (tokenToUse != null && !tokenToUse.isBlank()) {
            try {
                authService.logoutUser(new RefreshTokenRequest(tokenToUse));
            } catch (Exception e) {
                log.warn("Logout DB invalidation failed or token already expired. Continuing to clear cookie.");
            }
        }

        // 🟢 ALWAYS clear the client's cookie, even if the DB invalidation failed
        ResponseCookie cleanCookie = cookieUtils.getCleanRefreshCookie();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cleanCookie.toString())
                .body(new MessageResponse("Logged out successfully"));
    }

    @PostMapping("/logout-all")
    public ResponseEntity<MessageResponse> logoutAllUser() {
        MessageResponse response = authService.logoutFromAllDevices();

        // 🟢 Crucial: Clear the local browser cookie when signing out everywhere
        ResponseCookie cleanCookie = cookieUtils.getCleanRefreshCookie();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cleanCookie.toString())
                .body(response);
    }
}