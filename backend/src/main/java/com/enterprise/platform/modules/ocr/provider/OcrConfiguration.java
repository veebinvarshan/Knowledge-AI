package com.enterprise.platform.modules.ocr.provider;

import com.enterprise.platform.core.config.properties.TesseractProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;

@Configuration
@Conditional(OcrTesseractEnabledCondition.class)
@EnableConfigurationProperties(TesseractProperties.class)
public class OcrConfiguration {

    @Bean
    public TesseractOcrProvider tesseractOcrProvider(TesseractProperties properties) {
        return new TesseractOcrProvider(properties);
    }
}
