package com.verbrix.security.payload.response;

import lombok.Data;

import java.util.List;

@Data
public class LoginResponse {
    private String email;
    private List<String> roles;
    private String jwtToken; // This is the Access Token
    private String refreshToken;

    public LoginResponse(String email, List<String> roles, String jwtToken, String refreshToken) {
        this.email = email;
        this.roles = roles;
        this.jwtToken = jwtToken;
        this.refreshToken = refreshToken;
    }
}