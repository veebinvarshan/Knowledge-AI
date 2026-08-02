package com.enterprise.platform.infrastructure.health;

import com.enterprise.platform.core.config.properties.QdrantProperties;
import org.junit.jupiter.api.Test;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;

import static org.junit.jupiter.api.Assertions.*;

public class HealthIndicatorTest {

    @Test
    void testLivenessIndicatorAlwaysReturnsUp() {
        // GIVEN
        LivenessHealthIndicator indicator = new LivenessHealthIndicator();

        // WHEN
        Health health = indicator.health();

        // THEN
        assertEquals(Status.UP, health.getStatus());
    }

    @Test
    void testQdrantHealthIndicatorDisabledGracefullyUp() {
        // GIVEN
        QdrantProperties properties = new QdrantProperties(false, "localhost", 6334, 500, false, "key", "collection");
        QdrantHealthIndicator indicator = new QdrantHealthIndicator(properties);

        // WHEN
        Health health = indicator.health();

        // THEN
        assertEquals(Status.UP, health.getStatus());
        assertEquals("DISABLED", health.getDetails().get("status"));
    }

    @Test
    void testQdrantHealthIndicatorTimeoutReturnsDown() {
        // GIVEN (Points to a non-existent port with small timeout)
        QdrantProperties properties = new QdrantProperties(true, "192.0.2.1", 1234, 200, false, "key", "collection");
        QdrantHealthIndicator indicator = new QdrantHealthIndicator(properties);

        // WHEN
        Health health = indicator.health();

        // THEN (Must gracefully report DOWN rather than blocking/hanging)
        assertEquals(Status.DOWN, health.getStatus());
    }
}
