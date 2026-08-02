package com.enterprise.platform.modules.metadata.provider;

import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.HashMap;

@Component
public class GenericMetadataExtractor implements MetadataExtractor {

    @Override
    public boolean supports(String mimeType) {
        return true; // Catch-all fallback
    }

    @Override
    public int getPriority() {
        return 100; // Lowest priority
    }

    @Override
    public MetadataExtractionResult extract(InputStream inputStream) throws Exception {
        // Simple fallback parsing (no-op stream drain)
        byte[] buffer = new byte[4096];
        while (inputStream.read(buffer) != -1) {
            // Draining the stream to ensure proper cleanups
        }
        return new MetadataExtractionResult(
                "Generic File", null, "System", null, null, null,
                null, null, null,
                null, null, null, null, null,
                null, null, null, null,
                null, null, null,
                "UTF-8", null,
                new HashMap<>()
        );
    }
}
