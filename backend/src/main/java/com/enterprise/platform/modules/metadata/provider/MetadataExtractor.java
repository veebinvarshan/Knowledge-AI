package com.enterprise.platform.modules.metadata.provider;

import java.io.InputStream;

public interface MetadataExtractor {
    boolean supports(String mimeType);
    int getPriority(); // Lower values have higher selection priority (1 = MIME-specific, 2 = Tika-generic, 3 = Fallback)
    MetadataExtractionResult extract(InputStream inputStream) throws Exception;
}
