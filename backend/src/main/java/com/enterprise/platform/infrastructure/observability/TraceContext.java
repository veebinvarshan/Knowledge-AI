package com.enterprise.platform.infrastructure.observability;

public record TraceContext(
    String traceId,
    String spanId,
    boolean sampled
) {}
