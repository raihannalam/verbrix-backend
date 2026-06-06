package com.verbrix.payload.profile;

// Sent to the frontend to populate the profile page
public record UserProfileResponse(
    String firstName,
    String lastName,
    String email,
    String phone,
    String profilePictureUrl // Can be null for Clients initially
) {}