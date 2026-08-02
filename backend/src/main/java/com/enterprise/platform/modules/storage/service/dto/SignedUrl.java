package com.enterprise.platform.modules.storage.service.dto;

import java.time.Instant;

public record SignedUrl(
    String url,
    Instant expiration,
    String provider,
    String httpMethod
) {}
