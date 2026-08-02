package com.enterprise.platform.core.audit;

public interface AuditPublisher {
    void publish(AuditEvent event);
}
