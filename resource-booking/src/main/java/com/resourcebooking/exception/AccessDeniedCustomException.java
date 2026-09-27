package com.resourcebooking.exception;

// using a custom one instead of spring's AccessDeniedException in a couple spots
// because I wanted a plain message without setting up an entry point for it
public class AccessDeniedCustomException extends RuntimeException {
    public AccessDeniedCustomException(String message) {
        super(message);
    }
}
