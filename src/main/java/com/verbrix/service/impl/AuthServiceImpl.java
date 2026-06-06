package com.verbrix.service.impl;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseToken;
import com.verbrix.exception.*;
import com.verbrix.model.enums.RoleType;
import com.verbrix.model.profile.Client;
import com.verbrix.model.rbac.RefreshToken;
import com.verbrix.model.rbac.Role;
import com.verbrix.model.rbac.User;
import com.verbrix.model.security.UserDevice;
import com.verbrix.security.payload.response.SocialLoginRequest;
import com.verbrix.repository.*;
import com.verbrix.security.jwt.JwtUtils;
import com.verbrix.security.payload.request.*;
import com.verbrix.security.payload.response.LoginResponse;
import com.verbrix.security.payload.response.MessageResponse;
import com.verbrix.security.payload.response.RefreshTokenResponse;
import com.verbrix.security.service.RefreshTokenService;
import com.verbrix.security.service.SecurityAuditService;
import com.verbrix.security.service.UserDetailsImpl;
import com.verbrix.service.AuthService;
import com.verbrix.service.EmailService;
import com.verbrix.service.RealtimeEventPublisher;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

import com.verbrix.security.payload.response.OtpVerificationResponse;


@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final ClientRepository clientRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final RefreshTokenService refreshTokenService;
    private final RefreshTokenRepository refreshTokenRepository;
    private final EmailService emailService;
    private final SecurityAuditService securityAuditService;
    private final HttpServletRequest httpServletRequest;
    private final UserDeviceRepository  userDeviceRepository;
    private final RealtimeEventPublisher realtimeEventPublisher;


    @Override
    @Transactional
    public MessageResponse requestRegistration(EmailRequest emailRequest) {
        String email = emailRequest.getEmail();
        log.info("Registration OTP requested for {}", email);

        Optional<User> existing = userRepository.findByEmail(email);

        if (existing.isPresent() && existing.get().isActive()) {
            throw new EmailAlreadyExistsException("Error: Email is already in use.");
        }

        String otp = generateOtp();
        Instant expiry = Instant.now().plus(10, ChronoUnit.MINUTES);

        User user = existing.orElseGet(() ->
                User.builder()
                        .email(email)
                        .isActive(false)
                        .build()
        );

        user.setOtp(otp);
        user.setOtpExpiry(expiry);
        user.setOtpAttempts(0);
        user.setOtpLockedUntil(null);

        userRepository.save(user);
        emailService.sendOtpEmail(email, otp);

        return new MessageResponse("OTP sent successfully to " + email);
    }

    // --- COMPLETELY REWRITTEN verifyOtp ---
    // It now handles the OtpVerificationRequest and returns a token response
    @Override
    @Transactional
    public OtpVerificationResponse verifyOtp(OtpVerificationRequest request) {
        String email = request.getEmail();
        String otp = request.getOtp();
        log.info("OTP verification for {}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new InvalidCredentialsException("Invalid OTP or email."));

        // brute-force lockout
        if (user.getOtpLockedUntil() != null && user.getOtpLockedUntil().isAfter(Instant.now())) {
            throw new InvalidCredentialsException("Too many attempts. Try again later.");
        }

        if (user.getOtp() == null || !user.getOtp().equals(otp)) {
            int attempts = user.getOtpAttempts() + 1;
            user.setOtpAttempts(attempts);

            if (attempts >= 5) {
                user.setOtpLockedUntil(Instant.now().plus(15, ChronoUnit.MINUTES));
                user.setOtpAttempts(0);
            }

            userRepository.save(user);
            throw new InvalidCredentialsException("Invalid OTP or email.");
        }

        if (user.getOtpExpiry() == null || user.getOtpExpiry().isBefore(Instant.now())) {
            throw new InvalidCredentialsException("OTP expired.");
        }

        // success: clear OTP fields from DB
        user.setOtp(null);
        user.setOtpExpiry(null);
        user.setOtpAttempts(0);
        user.setOtpLockedUntil(null);
        // --- REMOVED user.setTempOtpVerified(true); ---

        userRepository.save(user);

        // --- NEW: Generate and return the Pre-Auth Token ---
        String preAuthToken = jwtUtils.generatePreAuthToken(user.getEmail());

        return new OtpVerificationResponse(
                "OTP verified successfully. Use this token to complete your request.",
                preAuthToken
        );
    }

    // ============================================================
    // 3. REGISTRATION — COMPLETE
    // ============================================================
    @Override
    @Transactional
    public LoginResponse completeRegistration(RegistrationRequest request) {
        log.info("Completing registration for {}", request.getEmail());

        // --- NEW: Validate the Pre-Auth Token ---
        if (!jwtUtils.validatePreAuthToken(request.getPreAuthToken())) {
            throw new InvalidCredentialsException("Invalid or expired pre-auth token.");
        }

        String emailFromToken = jwtUtils.getEmailFromPreAuthToken(request.getPreAuthToken());

        // Optional but recommended: cross-check token email with request email
        if (!emailFromToken.equals(request.getEmail())) {
            log.warn("Token/Request email mismatch: {} vs {}", emailFromToken, request.getEmail());
            throw new InvalidCredentialsException("Token and email mismatch.");
        }
        // ------------------------------------------

        User user = userRepository.findByEmail(emailFromToken)
                .orElseThrow(() -> new InvalidCredentialsException("Invalid token. User not found."));

        if (user.isActive()) {
            throw new EmailAlreadyExistsException("Error: Email already in use.");
        }

        // --- REMOVED check for user.isTempOtpVerified() ---

        user.setActive(true);
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        Role clientRole = roleRepository.findByName(RoleType.CLIENT)
                .orElseThrow(() -> new ResourceNotFoundException("Client role not found."));

        // --- FIX for UnsupportedOperationException ---
        // Must use a mutable Set (like HashSet) for JPA to manage
        Set<Role> roles = new HashSet<>();
        roles.add(clientRole);
        user.setRoles(roles);
        // ------------------------------------------

        User saved = userRepository.save(user);

        Client client = Client.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .user(saved)
                .build();
        clientRepository.save(client);

        UserDevice device = securityAuditService.recordDeviceLogin(saved, request.getDeviceId(),request.getDeviceDetails(), httpServletRequest);

        return loginAndReturnToken(saved, device);
    }

    // ============================================================
    // 4. LOGIN
    // ============================================================
    @Override
    public LoginResponse loginUser(LoginRequest loginRequest) {
        log.info("Login attempt for {}", loginRequest.getEmail());
        try {
            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getEmail(), loginRequest.getPassword()
                    ));

            SecurityContextHolder.getContext().setAuthentication(auth);
            UserDetailsImpl userDetails = (UserDetailsImpl) auth.getPrincipal();

            User user = userRepository.findByEmail(userDetails.getEmail())
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));

            UserDevice device = securityAuditService.recordDeviceLogin(user, loginRequest.getDeviceId(), loginRequest.getDeviceDetails(), httpServletRequest);

            return createTokens(userDetails, device);

        } catch (AuthenticationException e) {
            throw new InvalidCredentialsException("Invalid email or password!");
        }
    }

    // ============================================================
    // 5. SOCIAL LOGIN
    // ============================================================
    @Override
    @Transactional
    public LoginResponse loginWithSocialProvider(SocialLoginRequest socialLoginRequest) {
        log.info("Social login started");
        FirebaseToken decoded;

        try {
            decoded = FirebaseAuth.getInstance().verifyIdToken(socialLoginRequest.getIdToken());
        } catch (Exception e) {
            throw new SocialLoginException("Invalid social login token.");
        }

        String email = decoded.getEmail();
        if (email == null || email.isBlank()) {
            throw new SocialLoginException("Email not provided by provider.");
        }

        String provider = determineProvider(decoded); // Simplified method
        String uid = decoded.getUid();
        String name = decoded.getName();

        User user = userRepository.findByEmail(email).map(existing -> {
            existing.setAuthProvider(provider);
            existing.setProviderId(uid);
            return existing;
        }).orElseGet(() -> createSocialUser(email, provider, uid, name));

        UserDevice device = securityAuditService.recordDeviceLogin(user, socialLoginRequest.getDeviceId(), socialLoginRequest.getDeviceDetails(), httpServletRequest);
        return loginAndReturnToken(user,device);
    }

    // ============================================================
    // 6. REFRESH TOKEN
    // ============================================================
    @Override
    public RefreshTokenResponse refreshToken(RefreshTokenRequest request) {
        return refreshTokenRepository.findByToken(request.getRefreshToken())
                .map(refreshTokenService::verifyExpiration)
                .map(oldToken -> {
                    // FIX: Extract the user AND the device from the old token
                    User user = oldToken.getUser();
                    UserDevice device = oldToken.getDevice();

                    UserDetailsImpl details = UserDetailsImpl.build(user);
                    String newAccess = jwtUtils.generateAccessToken(details);

                    // FIX: Create the new token linked to the exact same device
                    RefreshToken newRefresh = refreshTokenService.createRefreshToken(user.getId(), device);

                    return new RefreshTokenResponse(newAccess, newRefresh.getToken());
                })
                .orElseThrow(() -> new TokenRefreshException("Refresh token not found."));
    }
    // ============================================================
    // 7. PASSWORD RESET — REQUEST OTP
    // ============================================================
    @Override
    @Transactional
    public MessageResponse requestPasswordReset(EmailRequest emailRequest) {
        String email = emailRequest.getEmail();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));

        if (!user.isActive()) {
            throw new InvalidCredentialsException("User is not active.");
        }

        String otp = generateOtp();
        user.setOtp(otp);
        user.setOtpExpiry(Instant.now().plus(10, ChronoUnit.MINUTES));

        // reset brute-force & flow flags
        user.setOtpAttempts(0);
        user.setOtpLockedUntil(null);

        userRepository.save(user);
        emailService.sendOtpEmail(email, otp);

        return new MessageResponse("Password reset OTP sent to " + email);
    }

    // ============================================================
    // 8. PASSWORD RESET — COMPLETE (requires prior OTP verification)
    // ============================================================
    @Override
    @Transactional
    public MessageResponse completePasswordReset(PasswordResetRequest request) {

        // --- NEW: Validate the Pre-Auth Token ---
        if (!jwtUtils.validatePreAuthToken(request.getPreAuthToken())) {
            throw new InvalidCredentialsException("Invalid or expired pre-auth token.");
        }

        String emailFromToken = jwtUtils.getEmailFromPreAuthToken(request.getPreAuthToken());
        // ------------------------------------------

        User user = userRepository.findByEmail(emailFromToken)
                .orElseThrow(() -> new InvalidCredentialsException("Invalid token. User not found."));

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));

        userRepository.save(user);
        return new MessageResponse("Password reset successfully. Please log in.");
    }

    // ============================================================
    // 9. LOGOUT
    // ============================================================
    @Override
    public MessageResponse logoutUser(RefreshTokenRequest refreshRequest) {
        // --- FIX: Only delete the specific token, not all user's tokens ---
        if (refreshRequest != null && refreshRequest.getRefreshToken() != null) {
            refreshTokenRepository.findByToken(refreshRequest.getRefreshToken())
                    .ifPresent(refreshTokenRepository::delete); // Changed from deleteByUserId
            return new MessageResponse("Logout successful.");
        }

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof UserDetailsImpl userDetails) {
            // This is a "log out everywhere" for the current user, which is fine
            refreshTokenService.deleteByUserId(userDetails.getId());
            SecurityContextHolder.clearContext();
        }

        return new MessageResponse("Logout successful.");
    }

    // ============================================================
    // HELPER METHODS
    // ============================================================
    private String generateOtp() {
        return String.valueOf(new SecureRandom().nextInt(900000) + 100000);
    }

    private LoginResponse loginAndReturnToken(User user, UserDevice device) {
        UserDetailsImpl details = UserDetailsImpl.build(user);
        return createTokens(details, device);
    }

    private LoginResponse createTokens(UserDetailsImpl userDetails, UserDevice device) {
        String access = jwtUtils.generateAccessToken(userDetails);
        RefreshToken refresh = refreshTokenService.createRefreshToken(userDetails.getId(), device);

        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        return new LoginResponse(
                userDetails.getEmail(),
                roles,
                access,
                refresh.getToken()
        );
    }

    private User createSocialUser(String email, String provider, String uid, String name) {
        Role clientRole = roleRepository.findByName(RoleType.CLIENT)
                .orElseThrow(() -> new ResourceNotFoundException("Client role not found."));


        Set<Role> roles = new HashSet<>();
        roles.add(clientRole);

        User newUser = User.builder()
                .email(email)
                .password(passwordEncoder.encode(UUID.randomUUID().toString()))
                .authProvider(provider)
                .providerId(uid)
                .roles(roles) // Use the mutable set
                .isActive(true)
                .build();

        User saved = userRepository.save(newUser);

        String first = (name != null && !name.isBlank()) ? name.split(" ")[0] : "User";
        String last = (name != null && name.contains(" "))
                ? name.substring(name.indexOf(" ") + 1)
                : ".";

        clientRepository.save(Client.builder()
                .firstName(first)
                .lastName(last)
                .user(saved)
                .build());

        return saved;
    }

    @Override
    @Transactional
    public MessageResponse logoutFromAllDevices() {
        // 1. Get the current user from Security Context
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) auth.getPrincipal();

        log.info("Performing Total Security Reset for: {}", userDetails.getEmail());

        // 2. Kill all active sessions (Refresh Tokens)
        refreshTokenRepository.deleteByUserId(userDetails.getId());

        // 3. Forget all known devices
        // This ensures the NEXT login from ANY device triggers a "New Login" alert
        userDeviceRepository.deleteAllByUserId(userDetails.getId());

        // 4. Clear current security context
        SecurityContextHolder.clearContext();

        return new MessageResponse("Logged out from all devices and device history cleared.");
    }

    // --- SIMPLIFIED based on user feedback (Google-only) ---
    private String determineProvider(FirebaseToken token) {
        Map<String, Object> claims = token.getClaims();
        if (claims.containsKey("firebase")) {
            Object providerObj = ((Map<?, ?>) claims.get("firebase")).get("sign_in_provider");
            if (providerObj != null) {
                String provider = providerObj.toString();
                // This will correctly return "google" for Google sign-in
                if (provider.contains("google")) {
                    return "google";
                }
            }
        }

        // Fallback check on issuer, just in case
        String issuer = token.getIssuer();
        if (issuer != null && issuer.contains("google")) {
            return "google";
        }

        return "unknown";
    }
}
