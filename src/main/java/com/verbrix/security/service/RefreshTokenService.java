package com.verbrix.security.service;

import com.verbrix.model.rbac.RefreshToken;
import com.verbrix.model.rbac.User;
import com.verbrix.model.security.UserDevice;
import com.verbrix.repository.RefreshTokenRepository;
import com.verbrix.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    @Value("${spring.app.jwtRefreshExpirationMs}")
    private Long refreshTokenDurationMs;

    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;

    @Transactional
    public RefreshToken createRefreshToken(Long userId, UserDevice device) {
        User user = userRepository.findById(userId).orElseThrow(
                () -> new RuntimeException("Error: User not found for refresh token")
        );

        // Crucial: If they log in again on the SAME device, invalidate the old token for that device!
        refreshTokenRepository.deleteByDeviceId(device.getId());

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setDevice(device); // Link it to the active device!
        refreshToken.setExpiryDate(Instant.now().plusMillis(refreshTokenDurationMs));
        refreshToken.setToken(UUID.randomUUID().toString());

        return refreshTokenRepository.save(refreshToken);
    }

    public RefreshToken verifyExpiration(RefreshToken token) {
        if (token.getExpiryDate().isBefore(Instant.now())) {
            refreshTokenRepository.delete(token);
            throw new RuntimeException("Refresh token was expired. Please make a new signin request");
        }
        return token;
    }

    @Transactional
    public void deleteByUserId(Long userId) {
        refreshTokenRepository.deleteByUserId(userId);
    }
}