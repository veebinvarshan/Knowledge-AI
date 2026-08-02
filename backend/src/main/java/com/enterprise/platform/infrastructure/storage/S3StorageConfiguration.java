package com.enterprise.platform.infrastructure.storage;

import com.enterprise.platform.core.config.properties.S3StorageProperties;
import com.enterprise.platform.modules.storage.service.StorageProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(name = "platform.storage.provider", havingValue = "s3")
@EnableConfigurationProperties(S3StorageProperties.class)
public class S3StorageConfiguration {

    @Bean
    public S3StorageProvider s3StorageProvider(S3StorageProperties properties) {
        return new S3StorageProvider(properties);
    }
}
