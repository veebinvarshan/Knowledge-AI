package com.enterprise.platform.modules.embedding.provider;

import java.util.List;

public record EmbeddingResult(
    List<float[]> embeddings,
    int dimensions,
    String modelName,
    String modelVersion
) {}
