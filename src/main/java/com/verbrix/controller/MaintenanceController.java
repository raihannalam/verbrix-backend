package com.verbrix.controller;

import com.verbrix.repository.UserRepository;
import com.verbrix.service.StorageService; // Import this
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@RestController
@RequiredArgsConstructor
@RequestMapping("/internal")
public class MaintenanceController {

    private final UserRepository userRepository;
    private final StorageService storageService; // Inject StorageService

    @PostMapping("/cleanup/users")
    public ResponseEntity<String> cleanupInactiveUsers(
            @RequestHeader("X-MAINTENANCE-KEY") String providedKey
    ) {
        validateKey(providedKey); // Refactored validation check
        Instant cutoff = Instant.now().minus(24, ChronoUnit.HOURS);
        int deletedCount = userRepository.deleteUnverifiedUsersOlderThan(cutoff);
        return ResponseEntity.ok("Cleanup completed. Deleted " + deletedCount + " unverified users.");
    }

    // --- NEW ENDPOINT ---
    @PostMapping("/cleanup/files")
    public ResponseEntity<String> cleanupOrphanedFiles(
            @RequestHeader("X-MAINTENANCE-KEY") String providedKey
    ) {
        validateKey(providedKey);

        int deletedCount = storageService.deleteOrphanedFiles();

        if (deletedCount == -1) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("File cleanup failed check logs.");
        }

        return ResponseEntity.ok("File cleanup completed. Deleted " + deletedCount + " orphaned files.");
    }

    // Helper method to avoid code duplication
    private void validateKey(String providedKey) {
        String expectedKey = System.getenv("MAINTENANCE_KEY");
        if (expectedKey == null || !expectedKey.equals(providedKey)) {
            throw new RuntimeException("Invalid Maintenance Key");
            // Better to use a specific exception and handler,
            // but for internal tools, throwing RuntimeException sends a 500 which fails the GitHub job (which is what you want).
        }
    }
}