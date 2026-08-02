package com.enterprise.platform.modules.authentication.api;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record RegisterRequest(
    @NotBlank(message = "Email is required")
    @Email(message = "Email must be well-formed")
    String email,

    @NotBlank(message = "Password is required")
    @Pattern(
        regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&#])[A-Za-z\\d@$!%*?&#]{12,128}$",
        message = "Password must be between 12 and 128 characters, containing at least one uppercase letter, one lowercase letter, one number, and one special character"
    )
    String password,

    @NotBlank(message = "Tenant domain is required")
    String tenant
) {}
