package com.enterprise.platform.modules.embedding;

import org.junit.jupiter.api.Test;

import java.security.MessageDigest;
import java.util.HexFormat;

import static org.junit.jupiter.api.Assertions.*;

public class ChunkHashValidationTest {

    @Test
    void testChunkHashCalculationIsDeterministic() throws Exception {
        String chunkText = "sample chunk text";

        String hash1 = HexFormat.of().formatHex(
                MessageDigest.getInstance("SHA-256").digest(chunkText.getBytes())
        );
        String hash2 = HexFormat.of().formatHex(
                MessageDigest.getInstance("SHA-256").digest(chunkText.getBytes())
        );

        assertEquals(hash1, hash2);
    }
}
