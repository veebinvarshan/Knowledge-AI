package com.enterprise.platform.modules.authentication.api;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
    @NotBlank(message = "Email is required")
    @Email(message = "Email must be well-formed")
    String email,

    @NotBlank(message = "Password is required")
    String password,

    @NotBlank(message = "Tenant domain is required")
    String tenant
) {}
