package com.carmanual.carmanual.services.exception;

public class DataBindingViolationException extends RuntimeException {
    public DataBindingViolationException(String message) {
        super(message);
    }

    public DataBindingViolationException(String message, Throwable cause) {
        super(message, cause);
    }
}