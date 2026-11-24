package com.businessmgmt.exception;

/**
 * Thrown when input validation fails.
 */
public class ValidationException extends BusinessException {

    private static final String ERROR_CODE = "VALIDATION_ERROR";

    /**
     * Creates a ValidationException with a message.
     *
     * @param message the error message
     */
    public ValidationException(String message) {
        super(message, ERROR_CODE);
    }

    /**
     * Creates a ValidationException with a message and cause.
     *
     * @param message the error message
     * @param cause   the underlying cause
     */
    public ValidationException(String message, Throwable cause) {
        super(message, ERROR_CODE, cause);
    }
}
