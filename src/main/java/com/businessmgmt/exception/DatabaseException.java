package com.businessmgmt.exception;

public class DatabaseException extends BusinessException {

    public DatabaseException(String message) {
        super(message, "DB_ERROR");
    }

    public DatabaseException(String message, Throwable cause) {
        super(message, "DB_ERROR", cause);
    }
}
