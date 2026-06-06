package com.verbrix.security.service;

import com.verbrix.model.rbac.User;
import com.verbrix.model.security.UserDevice;
import com.verbrix.repository.UserDeviceRepository;
import com.verbrix.security.service.helpers.DeviceMetadataHelper;
import com.verbrix.service.EmailService;
import com.verbrix.service.RealtimeEventPublisher;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class SecurityAuditService {

    private final UserDeviceRepository userDeviceRepository;
    private final DeviceMetadataHelper metadataHelper;
    private final EmailService emailService;
    private final RealtimeEventPublisher realtimeEventPublisher;

    @Transactional
    public UserDevice recordDeviceLogin(User user, String deviceId, String deviceDetails, HttpServletRequest request) {

        // Extract the true IP address (accounting for reverse proxies)
        String ip = metadataHelper.extractIp(request);

        // --- THE BULLETPROOF FALLBACK ---
        // If frontend didn't send details, intelligently extract it from the User-Agent header
        String finalDeviceDetails = (deviceDetails != null && !deviceDetails.isBlank())
                ? deviceDetails
                : metadataHelper.extractDeviceDetails(request);

        Optional<UserDevice> existingDevice = userDeviceRepository
                .findByUserAndDeviceIdentifier(user, deviceId);

        UserDevice activeDevice;

        if (existingDevice.isPresent()) {
            // KNOWN DEVICE: Update timestamp, IP, and device string
            activeDevice = existingDevice.get();
            activeDevice.setIpAddress(ip);
            activeDevice.setLastLogin(Instant.now());
            activeDevice.setDeviceDetails(finalDeviceDetails);

            activeDevice = userDeviceRepository.save(activeDevice);
            log.info("Login recorded for known device: {} for user: {}", finalDeviceDetails, user.getEmail());

        } else {
            // NEW DEVICE: Enrich data, Save, and Alert
            String location = metadataHelper.getCityAndCountry(ip);

            activeDevice = UserDevice.builder()
                    .user(user)
                    .deviceIdentifier(deviceId)
                    .deviceDetails(finalDeviceDetails)
                    .location(location)
                    .ipAddress(ip)
                    .lastLogin(Instant.now())
                    .isVerified(false)
                    .build();

            activeDevice = userDeviceRepository.save(activeDevice);

            // 1. Send Email Alert (Reliable fallback)
            emailService.sendNewLoginAlert(user.getEmail(), user.getEmail(), location, finalDeviceDetails);

            // 2. Send Realtime WebSocket Alert (Instant UI update)
            Map<String, String> alertPayload = Map.of(
                    "message", "A new login was detected on your account.",
                    "device", activeDevice.getDeviceDetails(),
                    "location", location != null ? location : "Unknown Location",
                    "time", Instant.now().toString(),
                    "ip", ip
            );
            realtimeEventPublisher.sendToUser(user.getEmail(), "SECURITY_ALERT_NEW_LOGIN", alertPayload);

            log.warn("New device detected! Security alerts (Email & WS) sent to {}", user.getEmail());
        }

        // Return the device so AuthServiceImpl can tie the RefreshToken to it safely
        return activeDevice;
    }
}