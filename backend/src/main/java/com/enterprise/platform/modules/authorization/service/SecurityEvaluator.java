package com.enterprise.platform.modules.authorization.service;

import java.util.UUID;

public interface SecurityEvaluator {
    boolean hasPermission(String permission);
    boolean hasPermission(String permission, UUID resourceId, String resourceType);
}
