package com.enterprise.platform.infrastructure.health;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Statement;

@Component
public class ReadinessHealthIndicator implements HealthIndicator {

    private final DataSource dataSource;
    private final StringRedisTemplate redisTemplate;
    private final ObjectProvider<QdrantHealthIndicator> qdrantHealthIndicatorProvider;

    public ReadinessHealthIndicator(
            DataSource dataSource,
            StringRedisTemplate redisTemplate,
            ObjectProvider<QdrantHealthIndicator> qdrantHealthIndicatorProvider) {
        this.dataSource = dataSource;
        this.redisTemplate = redisTemplate;
        this.qdrantHealthIndicatorProvider = qdrantHealthIndicatorProvider;
    }

    @Override
    public Health health() {
        boolean dbOk = checkPostgres();
        boolean redisOk = checkRedis();
        QdrantHealthIndicator qdrantIndicator = qdrantHealthIndicatorProvider.getIfAvailable();
        boolean qdrantOk = qdrantIndicator == null || 
                qdrantIndicator.health().getStatus().equals(org.springframework.boot.actuate.health.Status.UP);

        Health.Builder builder = Health.status(dbOk && redisOk && qdrantOk ? org.springframework.boot.actuate.health.Status.UP : org.springframework.boot.actuate.health.Status.DOWN)
                .withDetail("postgres", dbOk ? "UP" : "DOWN")
                .withDetail("redis", redisOk ? "UP" : "DOWN");

        if (qdrantIndicator != null) {
            builder.withDetail("qdrant", qdrantOk ? "UP" : "DOWN");
        } else {
            builder.withDetail("qdrant", "DISABLED");
        }

        return builder.build();
    }

    private boolean checkPostgres() {
        try (Connection conn = dataSource.getConnection()) {
            if (conn.isValid(3)) { // 3 seconds timeout
                try (Statement stmt = conn.createStatement()) {
                    stmt.execute("SELECT 1");
                    return true;
                }
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    private boolean checkRedis() {
        try {
            String ping = redisTemplate.execute(RedisConnection::ping);
            return "PONG".equalsIgnoreCase(ping);
        } catch (Exception e) {
            return false;
        }
    }
}
