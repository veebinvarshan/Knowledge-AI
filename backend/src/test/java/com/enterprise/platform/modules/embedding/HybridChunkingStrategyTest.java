package com.enterprise.platform.modules.embedding;

import com.enterprise.platform.core.config.properties.ChunkingProperties;
import com.enterprise.platform.modules.embedding.service.ChunkingService;
import com.enterprise.platform.modules.embedding.service.ChunkingStrategy;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class HybridChunkingStrategyTest {

    @Test
    void testHybridChunkingDecomposesLargeParagraphs() {
        ChunkingProperties properties = new ChunkingProperties("HYBRID", 50, 10);
        ChunkingService service = new ChunkingService(properties);

        String text = "Paragraph 1 is small.\n\nParagraph 2 is very long. It exceeds the max character count of fifty characters and should split.";

        // WHEN
        List<ChunkingService.Chunk> chunks = service.chunkText(text, ChunkingStrategy.HYBRID);

        // THEN
        assertFalse(chunks.isEmpty());
        // Verify paragraph 1 remains intact
        assertEquals("Paragraph 1 is small.", chunks.get(0).text());
        // Verify paragraph 2 is broken up
        assertTrue(chunks.size() > 1);
    }
}
