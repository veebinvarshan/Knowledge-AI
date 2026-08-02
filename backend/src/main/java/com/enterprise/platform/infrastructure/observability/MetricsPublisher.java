package com.enterprise.platform.infrastructure.observability;

public interface MetricsPublisher {
    void incrementCounter(String name, String... tags);
    void recordTimer(String name, long amountMs, String... tags);
}
