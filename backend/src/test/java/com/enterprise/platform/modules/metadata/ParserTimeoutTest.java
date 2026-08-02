package com.enterprise.platform.modules.metadata;

import com.enterprise.platform.core.config.properties.ApacheTikaProperties;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ParserTimeoutTest {

    @Test
    void testTimeoutLimitIsConfigured() {
        ApacheTikaProperties properties = new ApacheTikaProperties(1024L, 10, 10, 5000);
        assertEquals(5000, properties.parserTimeoutMs());
        assertEquals(1024L, properties.maxMetadataSize());
        assertEquals(10, properties.maxEmbeddedDepth());
        assertEquals(10, properties.maxRecursionDepth());
    }
}
