package com.truesurvey.exception;

/** Thrown when a record does not exist. Turned into HTTP 404 by GlobalExceptionHandler. */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
