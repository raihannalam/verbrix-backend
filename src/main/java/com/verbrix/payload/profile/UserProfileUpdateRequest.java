package com.verbrix.payload.profile;

public record UserProfileUpdateRequest(
    String firstName,
    String lastName,
    String phone,
    String profilePictureUrl
    // Email is intentionally omitted as it is read-only in the UI
) {}