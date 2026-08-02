package com.enterprise.platform.infrastructure.health;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

@Component
public class LivenessHealthIndicator implements HealthIndicator {

    @Override
    public Health health() {
        return Health.up().withDetail("status", "LIVENESS_OK").build();
    }
}
