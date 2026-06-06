package com.verbrix.controller;

import com.verbrix.model.enums.VerificationStatus;
import com.verbrix.payload.admininterpretercontroller.request.AdminRemarkRequest;
import com.verbrix.payload.admininterpretercontroller.response.ActionSuccessResponse;
import com.verbrix.payload.admininterpretercontroller.response.CertificationStatusResponse;
import com.verbrix.payload.admininterpretercontroller.response.InterpreterDetailResponse;
import com.verbrix.payload.admininterpretercontroller.response.InterpreterSummaryResponse;
import com.verbrix.service.AdminInterpreterService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/interpreters")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminInterpreterController {

    private final AdminInterpreterService adminService;

    /**
     * Get a list of interpreters, optionally filtered by status.
     * Used for the main admin dashboard table.
     */
    @GetMapping
    public ResponseEntity<List<InterpreterSummaryResponse>> getAllInterpreters(
            @RequestParam(required = false) VerificationStatus status) {
        return ResponseEntity.ok(adminService.getAllInterpreters(status));
    }

    /**
     * Get full details of a specific interpreter for review.
     */
    @GetMapping("/{id}")
    public ResponseEntity<InterpreterDetailResponse> getInterpreterDetails(@PathVariable Long id) {
        return ResponseEntity.ok(adminService.getInterpreterDetails(id));
    }

    /**
     * Approve an interpreter application and grant them access.
     */
    @PostMapping("/{id}/approve")
    public ResponseEntity<ActionSuccessResponse> approveInterpreter(@PathVariable Long id) {
        return ResponseEntity.ok(adminService.approveInterpreter(id));
    }

    /**
     * Ask the interpreter to fix specific issues and re-apply.
     */
    @PostMapping("/{id}/request-changes")
    public ResponseEntity<ActionSuccessResponse> requestChanges(
            @PathVariable Long id,
            @Valid @RequestBody AdminRemarkRequest request) {
        return ResponseEntity.ok(adminService.requestChanges(id, request));
    }

    /**
     * Permanently reject an interpreter application.
     */
    @PostMapping("/{id}/reject")
    public ResponseEntity<ActionSuccessResponse> rejectPermanently(
            @PathVariable Long id,
            @Valid @RequestBody AdminRemarkRequest request) {
        return ResponseEntity.ok(adminService.rejectPermanently(id, request));
    }

    /**
     * Approve a specific certification document.
     */
    @PatchMapping("/certifications/{certificationId}/verify")
    public ResponseEntity<CertificationStatusResponse> verifyCertification(
            @PathVariable Long certificationId) {
        return ResponseEntity.ok(adminService.verifyCertification(certificationId));
    }

    /**
     * Reject a specific certification document (e.g., blurry file, expired).
     */
    @PatchMapping("/certifications/{certificationId}/reject")
    public ResponseEntity<CertificationStatusResponse> rejectCertification(
            @PathVariable Long certificationId,
            @Valid @RequestBody AdminRemarkRequest request) {
        return ResponseEntity.ok(adminService.rejectCertification(certificationId, request));
    }
}