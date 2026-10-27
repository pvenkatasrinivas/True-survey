package com.truesurvey.exception;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Business-rule validation failed (rules that simple annotations can't express,
 * e.g. "a choice question needs at least 2 options" or "this question is required").
 * Turned into HTTP 400 with "fieldErrors".
 */
public class ValidationException extends RuntimeException {

    private final Map<String, String> fieldErrors;

    public ValidationException(String message, Map<String, String> fieldErrors) {
        super(message);
        this.fieldErrors = new LinkedHashMap<String, String>(fieldErrors);
    }

    public Map<String, String> getFieldErrors() {
        return fieldErrors;
    }
}
