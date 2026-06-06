package com.verbrix.payload;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CurrentUserInfoResponse {

    private Long id;
    private String email;
    private String firstName;
    private String lastName;
    private String role;
}
