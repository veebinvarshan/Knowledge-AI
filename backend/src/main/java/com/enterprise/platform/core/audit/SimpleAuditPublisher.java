package com.enterprise.platform.core.audit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class SimpleAuditPublisher implements AuditPublisher {

    private static final Logger log = LoggerFactory.getLogger(SimpleAuditPublisher.class);

    @Override
    public void publish(AuditEvent event) {
        log.info("AUDIT EVENT: Version: {}, Action: {}, Actor: {}, Tenant: {}, Request: {}, Entity: {}, EntityId: {}, Timestamp: {}",
                event.version(), event.action(), event.actorId(), event.tenantId(), event.requestId(), event.entity(), event.entityId(), event.timestamp());
    }
}
