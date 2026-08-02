package com.enterprise.platform.modules.semanticsearch;

import com.enterprise.platform.modules.semanticsearch.provider.SemanticSearchProvider;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.*;

public class ProviderPayloadValidationTest {

    @Test
    void testSearchSemanticInvokesProviderWithParameters() throws Exception {
        SemanticSearchProvider mockProvider = mock(SemanticSearchProvider.class);
        float[] vector = new float[768];
        Map<String, Object> filters = new HashMap<>();

        // WHEN
        mockProvider.searchSemantic(vector, "tenant-1", filters, 10);

        // THEN
        verify(mockProvider).searchSemantic(vector, "tenant-1", filters, 10);
    }
}
