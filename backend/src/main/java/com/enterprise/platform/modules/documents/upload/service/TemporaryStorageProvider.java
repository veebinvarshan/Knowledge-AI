package com.enterprise.platform.modules.documents.upload.service;

import com.enterprise.platform.modules.documents.upload.service.dto.TemporaryResource;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

public interface TemporaryStorageProvider {
    String getProviderId();
    void storeChunk(UUID sessionId, int chunkNumber, InputStream inputStream, long sizeBytes) throws IOException;
    TemporaryResource retrieveChunk(UUID sessionId, int chunkNumber) throws IOException;
    void deleteSessionData(UUID sessionId) throws IOException;
}
