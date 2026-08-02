package com.enterprise.platform.infrastructure.virusscan;

import com.enterprise.platform.core.config.properties.ClamAvProperties;
import com.enterprise.platform.modules.virusscan.provider.VirusScannerProvider;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;

@Configuration
@Conditional(ClamAvEnabledCondition.class)
@EnableConfigurationProperties(ClamAvProperties.class)
public class VirusScanConfiguration {

    @Bean
    public ClamAVScannerProvider clamAVScannerProvider(ClamAvProperties properties) {
        return new ClamAVScannerProvider(properties);
    }
}
