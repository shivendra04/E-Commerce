package com.scaler.userservicemwfeve.exceptions;

public class PasswordNotMatchingException extends RuntimeException {
    public PasswordNotMatchingException(String message) {
        super(message);
    }
}
