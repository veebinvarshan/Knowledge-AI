package com.enterprise.platform.modules.embedding;

import com.enterprise.platform.modules.search.provider.QdrantSearchProvider;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class QdrantPayloadValidationTest {

    @Test
    void testQdrantPayloadUpsertCalledWithCorrectParameters() throws Exception {
        QdrantSearchProvider qdrantProvider = mock(QdrantSearchProvider.class);

        UUID docId = UUID.randomUUID();
        UUID versionId = UUID.randomUUID();
        UUID chunkId = UUID.randomUUID();
        float[] vector = new float[768];

        // WHEN
        qdrantProvider.upsertVector(docId, versionId, "tenant-1", chunkId, 0, vector, "model", "v1", "checksum");

        // THEN
        verify(qdrantProvider).upsertVector(docId, versionId, "tenant-1", chunkId, 0, vector, "model", "v1", "checksum");
    }
}
