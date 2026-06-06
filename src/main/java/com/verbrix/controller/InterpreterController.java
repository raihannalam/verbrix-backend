package com.verbrix.controller;

import com.verbrix.payload.interpretercontroller.request.InterpreterApplicationRequest;
import com.verbrix.payload.interpretercontroller.request.SetConsultationFeesRequest;
import com.verbrix.payload.interpretercontroller.response.ApplicationStatusResponse;
import com.verbrix.payload.interpretercontroller.response.InterpreterApplicationResponse;
import com.verbrix.security.payload.response.MessageResponse;
import com.verbrix.service.InterpreterService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/interpreters")
@RequiredArgsConstructor
public class InterpreterController {

    private final InterpreterService interpreterService;

    @PostMapping("/apply")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<InterpreterApplicationResponse> apply(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody InterpreterApplicationRequest request
    ) {
        InterpreterApplicationResponse response =
                interpreterService.apply(userDetails.getUsername(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/re-apply")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<InterpreterApplicationResponse> reApply(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody InterpreterApplicationRequest request
    ) {
        InterpreterApplicationResponse response =
                interpreterService.reApply(userDetails.getUsername(), request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/me/application")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<InterpreterApplicationResponse> getMyApplication(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        // You'll need to add this method to your Service interface & impl
        InterpreterApplicationResponse response =
                interpreterService.getMyApplication(userDetails.getUsername());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/me/status")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApplicationStatusResponse> getApplicationStatus(
            @AuthenticationPrincipal UserDetails userDetails) {
        ApplicationStatusResponse response =
                interpreterService.getApplicationStatus(userDetails.getUsername());
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/set-consultation-fees")
    @PreAuthorize("hasRole('INTERPRETER')")
    public ResponseEntity<MessageResponse> updateConsultationFees(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody SetConsultationFeesRequest consultationFees
            ) {
        interpreterService.updateConsultationFees(consultationFees, userDetails.getUsername());
        return ResponseEntity.ok(new MessageResponse("Consultation fees updated successfully."));
    }
}