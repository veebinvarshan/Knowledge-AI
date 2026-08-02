package com.enterprise.platform.modules.authentication.api;

public record AuthResponse(
    String accessToken,
    String refreshToken,
    long expiresIn
) {}
