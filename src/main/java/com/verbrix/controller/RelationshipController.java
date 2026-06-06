package com.verbrix.controller;

import com.verbrix.payload.relationship.ConnectRequest;
import com.verbrix.payload.relationship.RelationshipActionRequest;
import com.verbrix.payload.relationship.RelationshipResponse;
import com.verbrix.service.RelationshipService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/relationships")
@RequiredArgsConstructor
public class RelationshipController {

    private final RelationshipService relationshipService;

    @PostMapping("/connect")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<RelationshipResponse> connect(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody ConnectRequest request) {
        
        return ResponseEntity.ok(
                relationshipService.connect(userDetails.getUsername(), request.getInterpreterProfileId(), request.getInitialMessage())
        );
    }

    @GetMapping("/requests/incoming")
    @PreAuthorize("hasRole('INTERPRETER')")
    public ResponseEntity<List<RelationshipResponse>> getIncomingRequests(
            @AuthenticationPrincipal UserDetails userDetails) {

        return ResponseEntity.ok(
                relationshipService.getIncomingRequests(userDetails.getUsername())
        );
    }

    @PatchMapping("/{id}/respond")
    @PreAuthorize("hasRole('INTERPRETER')")
    public ResponseEntity<RelationshipResponse> respondToRequest(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody RelationshipActionRequest request) {

        return ResponseEntity.ok(
                relationshipService.respondToConnectionRequest(id, userDetails.getUsername(), request)
        );
    }

    // Inside RelationshipController.java

    @GetMapping("/mine")
    @PreAuthorize("isAuthenticated()") // Allow both Clients and Interpreters
    public ResponseEntity<List<RelationshipResponse>> getMyConnections(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(
                relationshipService.getMyActiveRelationships(userDetails.getUsername())
        );
    }
}