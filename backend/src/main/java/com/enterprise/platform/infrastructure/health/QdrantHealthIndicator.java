package com.enterprise.platform.infrastructure.health;

import com.enterprise.platform.core.config.properties.QdrantProperties;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import java.net.InetSocketAddress;
import java.net.Socket;

public class QdrantHealthIndicator implements HealthIndicator {

    private final QdrantProperties properties;

    public QdrantHealthIndicator(QdrantProperties properties) {
        this.properties = properties;
    }

    @Override
    public Health health() {
        if (!properties.enabled()) {
            return Health.up().withDetail("status", "DISABLED").build();
        }

        try (Socket socket = new Socket()) {
            socket.connect(
                    new InetSocketAddress(properties.host(), properties.port()),
                    properties.connectionTimeoutMs()
            );
            return Health.up()
                    .withDetail("host", properties.host())
                    .withDetail("port", properties.port())
                    .build();
        } catch (Exception e) {
            return Health.down(e)
                    .withDetail("host", properties.host())
                    .withDetail("port", properties.port())
                    .withDetail("message", "Qdrant connection timed out or refused")
                    .build();
        }
    }
}
