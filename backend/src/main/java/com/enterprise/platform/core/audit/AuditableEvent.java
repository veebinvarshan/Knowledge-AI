package com.enterprise.platform.core.audit;

import java.time.Instant;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;

public interface AuditableEvent {
    default String getEventType() {
        return getClass().getSimpleName();
    }
    
    default Instant getTimestamp() {
        return Instant.now();
    }
    
    String getTenantId();
    
    default UUID getUserId() {
        return null;
    }
    
    default String getEntityType() {
        return "UNKNOWN";
    }
    
    default String getEntityId() {
        return null;
    }
    
    default String getAction() {
        return "UNKNOWN";
    }
    
    default Map<String, Object> getMetadata() {
        return Collections.emptyMap();
    }
}
