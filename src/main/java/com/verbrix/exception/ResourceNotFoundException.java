package com.verbrix.exception;

// We use this for critical config errors, like a missing Role
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}