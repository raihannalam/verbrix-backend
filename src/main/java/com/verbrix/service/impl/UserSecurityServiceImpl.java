package com.verbrix.service.impl;

import com.verbrix.exception.ResourceNotFoundException;
import com.verbrix.model.profile.Client;
import com.verbrix.model.profile.Interpreter;
import com.verbrix.model.rbac.User;
import com.verbrix.model.security.UserDevice;
import com.verbrix.payload.CurrentUserInfoResponse;
import com.verbrix.repository.RefreshTokenRepository;
import com.verbrix.repository.UserDeviceRepository;
import com.verbrix.repository.UserRepository;
import com.verbrix.security.jwt.JwtUtils;
import com.verbrix.service.UserSecurityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class UserSecurityServiceImpl implements UserSecurityService {

    private final UserDeviceRepository userDeviceRepository;
    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;


    @Override
    public List<UserDevice> getUserDevices(Long userId) {
        return userDeviceRepository.findByUserId(userId);
    }

    @Transactional
    @Override
    public void removeDevice(Long deviceId, Long userId) {

        refreshTokenRepository.deleteByDeviceId(deviceId);
        userDeviceRepository.deleteByDeviceIdAndUserId(deviceId, userId);

        log.info("Device {} removed for user {}", deviceId, userId);
    }


    @Override
    @Transactional(readOnly = true)
    public CurrentUserInfoResponse getCurrentUserInfo(Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        String role = user.getRoles()
                .stream()
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("User has no roles"))
                .getName()
                .toString();
        String firstName = null;
        String lastName = null;
        
        if ("CLIENT".equals(role)) {
            Client clientProfile = user.getClientProfile();
            if (clientProfile == null) {
                throw new ResourceNotFoundException("Client profile not found");
            }
            firstName = clientProfile.getFirstName();
            lastName = clientProfile.getLastName();

        } else if ("INTERPRETER".equals(role)) {
            Interpreter interpreterProfile = user.getInterpreterProfile();
            if (interpreterProfile == null) {
                throw new ResourceNotFoundException("Interpreter profile not found");
            }
            firstName = interpreterProfile.getFirstName();
            lastName = interpreterProfile.getLastName();
        
        } else if ("ADMIN".equals(role)) {
            firstName = "Admin";
            lastName = "";
        }

        return new CurrentUserInfoResponse(
                user.getId(),
                user.getEmail(),
                firstName,
                lastName,
                role
        );
    }
    
}