package com.enterprise.platform.modules.authentication.domain;

public class AuthenticationException extends RuntimeException {
    public AuthenticationException(String message) {
        super(message);
    }
}
