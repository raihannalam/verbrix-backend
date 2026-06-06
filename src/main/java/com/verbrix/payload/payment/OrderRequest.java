package com.verbrix.payload.payment;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class OrderRequest {
    @NotNull(message = "Relationship ID is required")
    private Long relationshipId;
}