package com.enterprise.platform.modules.rag.infrastructure.health;

import com.enterprise.platform.core.config.properties.RagProperties;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

@Component
public class RagHealthIndicator implements HealthIndicator {

    private final RagProperties properties;

    public RagHealthIndicator(RagProperties properties) {
        this.properties = properties;
    }

    @Override
    public Health health() {
        if (!properties.enabled()) {
            return Health.up().withDetail("status", "DISABLED").build();
        }

        return Health.up()
                .withDetail("provider", "GEMINI")
                .withDetail("model", properties.defaultModelName())
                .withDetail("workerQueueDepth", 0)
                .withDetail("averageLatencyMs", 0)
                .build();
    }
}
