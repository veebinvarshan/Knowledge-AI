package com.enterprise.platform.core.response;

public record ApiResponse(
    boolean success,
    String timestamp,
    String requestId,
    Object data
) {}
