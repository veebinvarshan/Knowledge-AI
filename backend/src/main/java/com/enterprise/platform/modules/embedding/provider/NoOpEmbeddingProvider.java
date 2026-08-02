package com.enterprise.platform.modules.embedding.provider;

import com.enterprise.platform.core.config.properties.EmbeddingProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class NoOpEmbeddingProvider implements EmbeddingProvider {

    private final EmbeddingProperties properties;

    public NoOpEmbeddingProvider(EmbeddingProperties properties) {
        this.properties = properties;
    }

    @Override
    public boolean supports(String provider) {
        return "NOOP".equalsIgnoreCase(provider);
    }

    @Override
    public int getPriority() {
        return 99; // Fallback provider
    }

    @Override
    public EmbeddingResult generate(List<String> chunks) {
        List<float[]> embeddings = new ArrayList<>();
        for (String chunk : chunks) {
            float[] mock = new float[768];
            int hash = chunk.hashCode();
            for (int k = 0; k < 768; k++) {
                mock[k] = (float) Math.cos(hash + k);
            }
            embeddings.add(mock);
        }
        return new EmbeddingResult(
                embeddings,
                768,
                properties.modelName(),
                properties.modelVersion()
        );
    }
}
