package com.enterprise.platform.modules.embedding;

import com.enterprise.platform.modules.embedding.domain.EmbeddingChunk;
import com.enterprise.platform.modules.embedding.service.EmbeddingVersionService;
import com.enterprise.platform.modules.embedding.repository.EmbeddingChunkRepository;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ModelUpgradeTest {

    @Test
    void testModelUpgradeRequiresReEmbedding() {
        EmbeddingChunkRepository mockRepository = mock(EmbeddingChunkRepository.class);
        EmbeddingVersionService service = new EmbeddingVersionService(mockRepository);

        UUID versionId = UUID.randomUUID();
        EmbeddingChunk chunk = EmbeddingChunk.builder()
                .documentId(UUID.randomUUID())
                .versionId(versionId)
                .chunkIndex(0)
                .tokenCount(10)
                .characterCount(40)
                .startOffset(0)
                .endOffset(40)
                .textHash("hash")
                .chunkHash("hash")
                .sourceVersion(versionId)
                .sourceChecksum("checksum")
                .sourceLength(40)
                .embeddingModel("model-v1")
                .embeddingModelVersion("1.0")
                .chunkText("text")
                .build();

        when(mockRepository.findAllByVersionId(versionId)).thenReturn(List.of(chunk));

        // WHEN: model upgrades
        boolean stale = service.areEmbeddingsStale(versionId, "model-v2", "2.0", "checksum");

        // THEN
        assertTrue(stale);
    }
}
