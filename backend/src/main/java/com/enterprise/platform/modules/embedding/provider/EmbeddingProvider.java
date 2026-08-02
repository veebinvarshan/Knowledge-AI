package com.enterprise.platform.modules.embedding.provider;

import java.util.List;

public interface EmbeddingProvider {
    boolean supports(String provider);
    EmbeddingResult generate(List<String> chunks);
    int getPriority();
}
