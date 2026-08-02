package com.enterprise.platform.infrastructure.observability;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class SimpleMetricsPublisher implements MetricsPublisher {

    private static final Logger log = LoggerFactory.getLogger(SimpleMetricsPublisher.class);

    @Override
    public void incrementCounter(String name, String... tags) {
        log.debug("Metric Counter Increment: {} with tags {}", name, tags);
    }

    @Override
    public void recordTimer(String name, long amountMs, String... tags) {
        log.debug("Metric Timer Record: {} ({} ms) with tags {}", name, amountMs, tags);
    }
}
