package com.ledgerlite.exception;

public class ValidationException extends LedgerException{

    public ValidationException(String message) {
        super(message);
    }

    public ValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}
