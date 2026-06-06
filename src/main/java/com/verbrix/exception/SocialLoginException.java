package com.verbrix.exception;

public class SocialLoginException extends RuntimeException {
    public SocialLoginException(String message) {
        super(message);
    }
}