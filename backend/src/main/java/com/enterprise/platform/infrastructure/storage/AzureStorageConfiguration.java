package com.enterprise.platform.infrastructure.storage;

import com.enterprise.platform.core.config.properties.AzureStorageProperties;
import com.enterprise.platform.modules.storage.service.StorageProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(name = "platform.storage.provider", havingValue = "azure")
@EnableConfigurationProperties(AzureStorageProperties.class)
public class AzureStorageConfiguration {

    @Bean
    public AzureBlobStorageProvider azureBlobStorageProvider(AzureStorageProperties properties) {
        return new AzureBlobStorageProvider(properties);
    }
}
