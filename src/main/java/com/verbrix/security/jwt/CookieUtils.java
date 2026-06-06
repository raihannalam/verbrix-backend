package com.verbrix.security.jwt;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

@Component
public class CookieUtils {

    @Value("${spring.app.jwtRefreshExpirationMs}")
    private int refreshTokenDurationMs;

    @Value("${app.cookie.domain}")
    private String cookieDomain;

    @Value("${app.cookie.secure}")
    private boolean cookieSecure;

    private static final String REFRESH_COOKIE_NAME = "verbrix_refresh";

    /**
     * Generates a secure HttpOnly cookie for the refresh token.
     */
    public ResponseCookie generateRefreshCookie(String refreshToken) {
        ResponseCookie.ResponseCookieBuilder builder = ResponseCookie.from(REFRESH_COOKIE_NAME, refreshToken)
                .httpOnly(true)
                .secure(cookieSecure)
                // 🟢 In Prod (Secure=true), "None" is required for cross-subdomain calls.
                // In Dev (Secure=false), "Lax" is required for standard HTTP.
                .sameSite(cookieSecure ? "None" : "Lax")
                .path("/")
                .maxAge(refreshTokenDurationMs / 1000);

        applyDomain(builder);

        return builder.build();
    }

    /**
     * Generates an expired cookie to clear the session on the client side.
     */
    public ResponseCookie getCleanRefreshCookie() {
        ResponseCookie.ResponseCookieBuilder builder = ResponseCookie.from(REFRESH_COOKIE_NAME, "")
                .httpOnly(true)
                .secure(cookieSecure)
                .sameSite(cookieSecure ? "None" : "Lax")
                .path("/")
                .maxAge(0);

        applyDomain(builder);

        return builder.build();
    }

    /**
     * 🟢 Handles domain logic for both Local IP and Production (.verbrix.com)
     */
    // CookieUtils.java
    private void applyDomain(ResponseCookie.ResponseCookieBuilder builder) {
        if (cookieDomain != null &&
                !cookieDomain.isBlank() &&
                !cookieDomain.equalsIgnoreCase("localhost") &&
                !isIpAddress(cookieDomain)) {

            // 🟢 FIX: For production subdomains (api.verbrix.com <-> verbrix.com),
            // the domain MUST start with a dot to be shared across subdomains.
            String domainToSet = cookieDomain.startsWith(".") ? cookieDomain : "." + cookieDomain;
            builder.domain(domainToSet);
        }
    }

    private boolean isIpAddress(String domain) {
        return domain.matches("^(\\d{1,3}\\.){3}\\d{1,3}$");
    }
}