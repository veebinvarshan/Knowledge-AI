package com.enterprise.platform.modules.embedding.provider;

import com.enterprise.platform.core.config.properties.EmbeddingProperties;
import com.enterprise.platform.core.config.properties.GeminiEmbeddingProperties;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;

@Configuration
@Conditional(EmbeddingGeminiEnabledCondition.class)
@EnableConfigurationProperties(GeminiEmbeddingProperties.class)
public class EmbeddingConfiguration {

    @Bean
    public GeminiEmbeddingProvider geminiEmbeddingProvider(
            ObjectProvider<EmbeddingModel> embeddingModelProvider,
            EmbeddingProperties embeddingProperties,
            GeminiEmbeddingProperties geminiProperties) {
        return new GeminiEmbeddingProvider(embeddingModelProvider, embeddingProperties, geminiProperties);
    }
}
