package com.verbrix.payload.relationship;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RelationshipActionRequest {
    
    @NotNull(message = "Action is required")
    private Action action;

    public enum Action {
        ACCEPT,
        REJECT
    }
}