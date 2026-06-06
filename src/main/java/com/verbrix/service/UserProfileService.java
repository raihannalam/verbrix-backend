package com.verbrix.service;

import com.verbrix.payload.profile.PasswordUpdateRequest;
import com.verbrix.payload.profile.UserProfileResponse;
import com.verbrix.payload.profile.UserProfileUpdateRequest;
import org.springframework.transaction.annotation.Transactional;

public interface UserProfileService {

    @Transactional(readOnly = true)
    UserProfileResponse getProfile(String email);

    @Transactional
    UserProfileResponse updateProfile(String email, UserProfileUpdateRequest request);

    @Transactional
    void updatePassword(String email, PasswordUpdateRequest request);
}
