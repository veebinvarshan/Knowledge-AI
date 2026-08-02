package com.enterprise.platform.infrastructure.storage;

import com.enterprise.platform.core.config.properties.MinioStorageProperties;
import com.enterprise.platform.modules.storage.service.StorageProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(name = "platform.storage.provider", havingValue = "minio")
@EnableConfigurationProperties(MinioStorageProperties.class)
public class MinioStorageConfiguration {

    @Bean
    public MinIOStorageProvider minioStorageProvider(MinioStorageProperties properties) {
        return new MinIOStorageProvider(properties);
    }
}
