package com.enterprise.platform.modules.authentication.domain;

public class AccountLockedException extends AuthenticationException {
    public AccountLockedException(String message) {
        super(message);
    }
}
