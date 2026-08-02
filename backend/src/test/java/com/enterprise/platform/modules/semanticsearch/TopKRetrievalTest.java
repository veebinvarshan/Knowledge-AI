package com.enterprise.platform.modules.semanticsearch;

import com.enterprise.platform.modules.semanticsearch.domain.SemanticSearchRequest;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class TopKRetrievalTest {

    @Test
    void testRequestLimitSetting() {
        SemanticSearchRequest request = new SemanticSearchRequest(
                "query", UUID.randomUUID(), Collections.emptyMap(), 15
        );

        assertEquals(15, request.limit());
    }
}
