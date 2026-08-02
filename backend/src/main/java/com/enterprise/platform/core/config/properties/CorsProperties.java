package com.enterprise.platform.core.config.properties;

import jakarta.validation.constraints.NotEmpty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;
import java.util.List;

@ConfigurationProperties(prefix = "platform.cors")
@Validated
public record CorsProperties(
    @NotEmpty List<String> allowedOrigins,
    @NotEmpty List<String> allowedMethods,
    @NotEmpty List<String> allowedHeaders,
    boolean allowCredentials,
    long maxAgeSeconds
) {}
