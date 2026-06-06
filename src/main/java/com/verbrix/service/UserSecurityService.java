package com.verbrix.service;

import com.verbrix.model.security.UserDevice;
import com.verbrix.payload.CurrentUserInfoResponse;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface UserSecurityService {
    List<UserDevice> getUserDevices(Long userId);

    @Transactional
    void removeDevice(Long deviceId, Long userId);

    CurrentUserInfoResponse getCurrentUserInfo(Long userId);
}
