package com.verbrix.payload.relationship;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ConnectRequest {
    
    @NotNull(message = "Interpreter ID is required")
    private Long interpreterProfileId;

    @NotBlank(message = "Initial message is required")
    private String initialMessage; // e.g., "Hi, I need help with a liver transplant visa."
}