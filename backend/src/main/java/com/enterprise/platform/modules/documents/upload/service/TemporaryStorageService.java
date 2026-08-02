package com.enterprise.platform.modules.documents.upload.service;

import com.enterprise.platform.modules.documents.upload.service.dto.TemporaryResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

@Service
public class TemporaryStorageService {

    private final TemporaryStorageProvider provider;

    public TemporaryStorageService(TemporaryStorageProvider provider) {
        this.provider = provider;
    }

    public void storeChunk(UUID sessionId, int chunkNumber, InputStream inputStream, long sizeBytes) throws IOException {
        provider.storeChunk(sessionId, chunkNumber, inputStream, sizeBytes);
    }

    public TemporaryResource retrieveChunk(UUID sessionId, int chunkNumber) throws IOException {
        return provider.retrieveChunk(sessionId, chunkNumber);
    }

    public void deleteSessionData(UUID sessionId) throws IOException {
        provider.deleteSessionData(sessionId);
    }
}
