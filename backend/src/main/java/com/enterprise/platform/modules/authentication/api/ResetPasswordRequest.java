package com.enterprise.platform.modules.authentication.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record ResetPasswordRequest(
    @NotBlank(message = "Token is required")
    String token,

    @NotBlank(message = "New password is required")
    @Pattern(
        regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&#])[A-Za-z\\d@$!%*?&#]{12,128}$",
        message = "Password must be between 12 and 128 characters, containing at least one uppercase letter, one lowercase letter, one number, and one special character"
    )
    String newPassword
) {}
