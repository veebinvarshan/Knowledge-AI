package com.enterprise.platform.infrastructure.storage;

import com.enterprise.platform.core.config.properties.LocalStorageProperties;
import com.enterprise.platform.modules.storage.service.StorageProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(name = "platform.storage.provider", havingValue = "local", matchIfMissing = true)
@EnableConfigurationProperties(LocalStorageProperties.class)
public class LocalStorageConfiguration {

    @Bean
    public LocalStorageProvider localStorageProvider(LocalStorageProperties properties) {
        return new LocalStorageProvider(properties);
    }
}
