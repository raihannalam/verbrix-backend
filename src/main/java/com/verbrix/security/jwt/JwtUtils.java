package com.verbrix.security.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SecurityException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtUtils {

    private static final Logger logger = LoggerFactory.getLogger(JwtUtils.class);

    @Value("${spring.app.jwtSecret}")
    private String jwtSecret;

    @Value("${spring.app.jwtAccessExpirationMs}")
    private Long jwtAccessExpirationMs;

    // --- NEW ---
    // 5-minute expiration for Pre-Auth "hall pass" tokens
    private static final long PRE_AUTH_EXPIRATION_MS = 300000;

    private SecretKey key() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret));
    }

    public String generateAccessToken(UserDetails userDetails) {
        return Jwts.builder()
                .subject(userDetails.getUsername())
                .claim("roles", userDetails.getAuthorities()
                        .stream().map(GrantedAuthority::getAuthority).toList())
                .issuedAt(Date.from(Instant.now()))
                .expiration(Date.from(Instant.now().plusMillis(jwtAccessExpirationMs)))
                .signWith(key())
                .compact();
    }

    // ... (getJwtFromHeader and getUsernameFromJwtToken remain unchanged) ...

    public String getJwtFromHeader(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        logger.debug("No Bearer Token found in Authorization header");
        return null;
    }

    public String getUsernameFromJwtToken(String token) {
        return Jwts.parser()
                .verifyWith(key())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    public boolean validateJwtToken(String authToken) {
        try {
            Jwts.parser()
                    .verifyWith(key())
                    .build()
                    .parseSignedClaims(authToken);
            return true;
        } catch (SecurityException | MalformedJwtException e) {
            logger.error("Invalid JWT Token: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            logger.warn("Expired JWT Token: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            logger.error("Unsupported JWT Token: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            logger.error("JWT claims string is empty or null: {}", e.getMessage());
        }
        return false;
    }

    // ============================================================
    // --- NEW METHODS FOR PRE-AUTH TOKEN ---
    // ============================================================

    /**
     * Generates a short-lived, single-purpose Pre-Auth token.
     */
    public String generatePreAuthToken(String email) {
        return Jwts.builder()
                .subject(email)
                // Critical: This claim distinguishes it from a real access token
                .claim("type", "PRE_AUTH")
                .issuedAt(Date.from(Instant.now()))
                .expiration(Date.from(Instant.now().plusMillis(PRE_AUTH_EXPIRATION_MS)))
                .signWith(key())
                .compact();
    }

    /**
     * Validates a Pre-Auth token (checks signature, expiration, and type).
     */
    public boolean validatePreAuthToken(String authToken) {
        try {
            Jws<Claims> claims = Jwts.parser()
                    .verifyWith(key())
                    .build()
                    .parseSignedClaims(authToken);

            // Critical: Check that it's a "PRE_AUTH" token, not a stolen Access Token
            String type = claims.getPayload().get("type", String.class);
            if (!"PRE_AUTH".equals(type)) {
                logger.error("Invalid JWT type: Expected 'PRE_AUTH', got '{}'", type);
                return false;
            }

            return true;

        } catch (JwtException | IllegalArgumentException e) {
            logger.error("Pre-Auth JWT validation failed: {}", e.getMessage());
        }
        return false;
    }

    /**
     * Gets the email from a Pre-Auth token.
     * Note: Assumes token is already validated.
     */
    public String getEmailFromPreAuthToken(String token) {
        return Jwts.parser()
                .verifyWith(key())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }
//    public List<String> getRolesFromJwtToken(String token) {
//        Claims claims = Jwts.parser()
//                .verifyWith(key())
//                .build()
//                .parseSignedClaims(token)
//                .getPayload();
//
//        return claims.get("roles", List.class);
//    }
}