package com.main.exception;

public class RequestValidationException extends RuntimeException {
    public RequestValidationException(String message) {
        super(message);
    }
    public RequestValidationException(Throwable t) {
        super(t);
    }
}
