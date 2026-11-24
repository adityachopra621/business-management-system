package com.businessmgmt.exception;

/**
 * Base exception for all business-logic errors in the application.
 * Carries an optional error code for programmatic handling.
 */
public class BusinessException extends RuntimeException {

    private final String errorCode;

    /**
     * Creates a BusinessException with a message and a default error code.
     *
     * @param message the error message
     */
    public BusinessException(String message) {
        super(message);
        this.errorCode = "BIZ_ERROR";
    }

    /**
     * Creates a BusinessException with a message and an explicit error code.
     *
     * @param message   the error message
     * @param errorCode the error code
     */
    public BusinessException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    /**
     * Creates a BusinessException with a message and a cause.
     *
     * @param message the error message
     * @param cause   the underlying cause
     */
    public BusinessException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = "BIZ_ERROR";
    }

    /**
     * Creates a BusinessException with a message, error code, and cause.
     *
     * @param message   the error message
     * @param errorCode the error code
     * @param cause     the underlying cause
     */
    public BusinessException(String message, String errorCode, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    /**
     * Returns the error code associated with this exception.
     *
     * @return the error code
     */
    public String getErrorCode() {
        return errorCode;
    }
}
