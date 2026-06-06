package com.verbrix.controller;

import com.verbrix.model.security.UserDevice;
import com.verbrix.payload.CurrentUserInfoResponse;
import com.verbrix.security.payload.response.MessageResponse;
import com.verbrix.security.service.UserDetailsImpl;
import com.verbrix.service.UserSecurityService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/user/security")
@PreAuthorize("isAuthenticated()")
@RequiredArgsConstructor
public class UserSecurityController {

    private final UserSecurityService userSecurityService;

    @GetMapping("/me")
    public ResponseEntity<CurrentUserInfoResponse> getCurrentUser(
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        return ResponseEntity.ok(
                userSecurityService.getCurrentUserInfo(userDetails.getId())
        );
    }

    @GetMapping("/active-devices")
    public ResponseEntity<List<UserDevice>> getActiveDevices(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        // Retrieve devices for the currently authenticated user
        return ResponseEntity.ok(userSecurityService.getUserDevices(userDetails.getId()));
    }
    @DeleteMapping("/devices/{deviceId}")
    public ResponseEntity<MessageResponse> removeDevice(
            @PathVariable Long deviceId,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {

        userSecurityService.removeDevice(deviceId, userDetails.getId());

        return ResponseEntity.ok(new MessageResponse("Device removed successfully."));
    }
}