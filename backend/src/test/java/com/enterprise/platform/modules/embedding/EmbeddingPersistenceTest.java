package com.enterprise.platform.modules.embedding;

import com.enterprise.platform.modules.embedding.domain.EmbeddingChunk;
import com.enterprise.platform.modules.embedding.repository.EmbeddingChunkRepository;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class EmbeddingPersistenceTest {

    @Test
    void testChunkPersistenceMock() {
        EmbeddingChunkRepository mockRepository = mock(EmbeddingChunkRepository.class);
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
                .embeddingModel("model")
                .embeddingModelVersion("v1")
                .chunkText("text")
                .build();

        when(mockRepository.findAllByVersionId(versionId)).thenReturn(List.of(chunk));

        // WHEN
        List<EmbeddingChunk> results = mockRepository.findAllByVersionId(versionId);

        // THEN
        assertEquals(1, results.size());
        assertEquals("text", results.get(0).getChunkText());
    }
}
