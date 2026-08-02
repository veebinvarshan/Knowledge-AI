package com.enterprise.platform.modules.embedding.service;

import com.enterprise.platform.modules.embedding.domain.EmbeddingChunk;
import com.enterprise.platform.modules.embedding.repository.EmbeddingChunkRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class EmbeddingVersionService {

    private final EmbeddingChunkRepository chunkRepository;

    public EmbeddingVersionService(EmbeddingChunkRepository chunkRepository) {
        this.chunkRepository = chunkRepository;
    }

    public boolean areEmbeddingsStale(
            UUID versionId, 
            String currentModel, 
            String currentModelVersion, 
            String currentSourceChecksum) {
        
        List<EmbeddingChunk> chunks = chunkRepository.findAllByVersionId(versionId);
        if (chunks.isEmpty()) {
            return true; // No embeddings exist yet
        }

        EmbeddingChunk first = chunks.get(0);
        return !first.getEmbeddingModel().equals(currentModel)
                || !first.getEmbeddingModelVersion().equals(currentModelVersion)
                || !first.getSourceChecksum().equals(currentSourceChecksum);
    }
}
