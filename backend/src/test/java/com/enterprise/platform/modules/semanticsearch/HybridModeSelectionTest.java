package com.enterprise.platform.modules.semanticsearch;

import com.enterprise.platform.core.config.properties.SemanticSearchProperties;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class HybridModeSelectionTest {

    @Test
    void testHybridModeConfigurationProperties() {
        SemanticSearchProperties properties = new SemanticSearchProperties(true, "HYBRID", "QDRANT", "COSINE", 10, false, 60000);
        assertEquals("HYBRID", properties.mode());
    }
}
