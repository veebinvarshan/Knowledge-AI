package com.enterprise.platform.infrastructure.observability;

public record RequestContext(
    String method,
    String uri,
    String clientIp
) {}
