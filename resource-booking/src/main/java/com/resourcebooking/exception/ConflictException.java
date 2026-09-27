package com.resourcebooking.exception;

// for stuff that's valid on its own but conflicts with existing data - e.g. booking
// a resource that's already reserved for that time slot. Maps to 409 in GlobalExceptionHandler
public class ConflictException extends RuntimeException {
    public ConflictException(String message) {
        super(message);
    }
}
