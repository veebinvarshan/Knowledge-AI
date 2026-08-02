package com.enterprise.platform.modules.search.provider;

import com.enterprise.platform.core.config.properties.QdrantProperties;
import com.enterprise.platform.infrastructure.health.QdrantHealthIndicator;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(name = "platform.search.qdrant.enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(QdrantProperties.class)
public class QdrantSearchConfiguration {

    @Bean
    public QdrantSearchProvider qdrantSearchProvider(QdrantProperties properties) {
        return new QdrantSearchProvider(properties);
    }

    @Bean
    public HealthIndicator qdrantHealthIndicator(QdrantProperties properties) {
        return new QdrantHealthIndicator(properties);
    }
}
