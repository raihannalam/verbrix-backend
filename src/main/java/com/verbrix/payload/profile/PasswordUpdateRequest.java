package com.verbrix.payload.profile;

public record PasswordUpdateRequest(
    String currentPassword,
    String newPassword
) {}