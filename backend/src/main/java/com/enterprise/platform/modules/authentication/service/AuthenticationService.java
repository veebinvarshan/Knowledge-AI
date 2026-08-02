package com.enterprise.platform.modules.authentication.service;

import com.enterprise.platform.modules.authentication.api.*;

public interface AuthenticationService {
    AuthResponse register(RegisterRequest request);
    AuthResponse login(LoginRequest request, String ipAddress, String userAgent, String fingerprint);
    void logout(String refreshToken);
    AuthResponse refresh(String refreshToken, String fingerprint);
    boolean validateSession(String accessToken);
    void forgotPassword(String email);
    void resetPassword(ResetPasswordRequest request);
    void verifyEmail(String token);
    void changePassword(String email, ChangePasswordRequest request);
}
