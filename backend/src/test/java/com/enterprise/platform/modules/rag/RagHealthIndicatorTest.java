package com.enterprise.platform.modules.rag;

import com.enterprise.platform.core.config.properties.RagProperties;
import com.enterprise.platform.modules.rag.infrastructure.health.RagHealthIndicator;
import org.junit.jupiter.api.Test;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;

import static org.junit.jupiter.api.Assertions.*;

public class RagHealthIndicatorTest {

    @Test
    void testHealthOutputs() {
        RagProperties properties = new RagProperties(
                true, "gemini-2.5-flash", 2000, 0.2, true, "system prompt"
        );
        RagHealthIndicator indicator = new RagHealthIndicator(properties);

        // WHEN
        Health health = indicator.health();

        // THEN
        assertEquals(Status.UP, health.getStatus());
        assertEquals("GEMINI", health.getDetails().get("provider"));
    }
}
