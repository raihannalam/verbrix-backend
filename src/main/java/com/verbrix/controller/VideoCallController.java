package com.verbrix.controller;

import com.verbrix.model.business.booking.Relationship;
import com.verbrix.model.enums.RelationshipStatus;
import com.verbrix.repository.RelationshipRepository;
import com.verbrix.service.LiveKitService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/video")
@RequiredArgsConstructor
public class VideoCallController {

    private final RelationshipRepository relationshipRepository;
    private final LiveKitService liveKitService;

    /**
     * Join a video call (relationship-scoped, WhatsApp-style)
     */
    @PostMapping("/{relationshipId}/join")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<String> joinCall(
            @PathVariable Long relationshipId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        Relationship relationship = relationshipRepository.findRelationshipWithUsers(relationshipId)
                .orElse(null);

        if (relationship == null) return ResponseEntity.notFound().build();

        // 1. Check Participant
        if (!relationship.isParticipant(userDetails.getUsername())) {
            return ResponseEntity.status(403).build();
        }

        // 2. 🟢 Check Status Explicitly (More robust than isCallingAllowed helper)
        boolean canCall = relationship.getStatus() == RelationshipStatus.CONSULTATION_ACTIVE
                || relationship.getStatus() == RelationshipStatus.AGREEMENT_ACTIVE
                || relationship.getStatus() == RelationshipStatus.WORK_ACTIVE;

        if (!canCall) {
            return ResponseEntity.status(409).body("Call not allowed in status: " + relationship.getStatus());
        }

        String token = liveKitService.generateCallToken(
                relationship.getId(),
                userDetails.getUsername()
        );

        return ResponseEntity.ok(token);
    }
}