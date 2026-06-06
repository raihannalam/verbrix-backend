package com.verbrix.payload.admininterpretercontroller.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminRemarkRequest {
    
    @NotBlank(message = "Reason/Message cannot be empty")
    @Size(min = 5, max = 1000, message = "Message must be between 5 and 1000 characters")
    private String message;
}