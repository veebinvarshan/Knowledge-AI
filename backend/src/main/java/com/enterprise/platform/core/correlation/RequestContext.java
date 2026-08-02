package com.enterprise.platform.core.correlation;

import java.util.UUID;

public interface RequestContext {
    String getRequestId();
    String getTenantId();
    UUID getAuthenticatedUserId();
    String getClientIp();
    String getUserAgent();
}
