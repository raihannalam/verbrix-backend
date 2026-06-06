package com.verbrix.security.payload.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class UserProfileResponse {
    private String email;
    private String firstName;
    private String lastName;
    private String profileType;
    private boolean active;
    private List<String> roles;
}
