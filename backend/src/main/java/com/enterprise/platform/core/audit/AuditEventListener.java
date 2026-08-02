package com.enterprise.platform.core.audit;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class AuditEventListener {

    private final AuditPublisher auditPublisher;

    public AuditEventListener(AuditPublisher auditPublisher) {
        this.auditPublisher = auditPublisher;
    }

    @EventListener
    public void onAuditableEvent(AuditableEvent event) {
        AuditEvent auditEvent = new AuditEvent(
                event.getUserId(),
                event.getTenantId(),
                null,
                event.getAction(),
                event.getEntityType(),
                event.getEntityId()
        );
        auditPublisher.publish(auditEvent);
    }
}
