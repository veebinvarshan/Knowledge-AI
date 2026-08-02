package com.enterprise.platform.modules.authorization.service;

import java.util.UUID;

public interface ResourceHierarchyProvider {
    /**
     * Return true if this provider supports the resource type.
     */
    boolean supports(String resourceType);

    /**
     * Get parent resource ID for a resource, or null if none.
     */
    UUID getParentId(UUID resourceId);

    /**
     * Get parent resource type (e.g. 'FOLDER', 'KNOWLEDGE_BASE'), or null if none.
     */
    String getParentType(UUID resourceId);

    /**
     * Get the owner ID of the resource. Returns null if unknown.
     */
    UUID getOwnerId(UUID resourceId);
}
