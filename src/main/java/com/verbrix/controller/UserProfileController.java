package com.verbrix.controller;

import com.verbrix.payload.profile.PasswordUpdateRequest;
import com.verbrix.payload.profile.UserProfileResponse;
import com.verbrix.payload.profile.UserProfileUpdateRequest;
import com.verbrix.service.UserProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users/me")
@RequiredArgsConstructor
public class UserProfileController {

    private final UserProfileService userProfileService;

    @GetMapping
    public ResponseEntity<UserProfileResponse> getMyProfile(
            @AuthenticationPrincipal UserDetails userDetails) {
        
        // Pass the authenticated user's email or ID to the service
        UserProfileResponse response = userProfileService.getProfile(userDetails.getUsername());
        return ResponseEntity.ok(response);
    }

    @PutMapping
    public ResponseEntity<UserProfileResponse> updateMyProfile(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody UserProfileUpdateRequest request) {
        
        UserProfileResponse updatedProfile = userProfileService.updateProfile(userDetails.getUsername(), request);
        return ResponseEntity.ok(updatedProfile);
    }

    @PutMapping("/password")
    public ResponseEntity<Void> updatePassword(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody PasswordUpdateRequest request) {
        
        userProfileService.updatePassword(userDetails.getUsername(), request);
        return ResponseEntity.noContent().build();
    }
}