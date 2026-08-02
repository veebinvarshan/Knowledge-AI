package com.enterprise.platform.infrastructure.upload;

import com.enterprise.platform.core.config.properties.LocalStorageProperties;
import com.enterprise.platform.modules.documents.upload.service.TemporaryStorageProvider;
import com.enterprise.platform.modules.documents.upload.service.dto.TemporaryResource;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.file.*;
import java.util.UUID;

@Component
public class LocalTemporaryStorageProvider implements TemporaryStorageProvider {

    private final Path tempRootPath;

    public LocalTemporaryStorageProvider(LocalStorageProperties properties) {
        this.tempRootPath = Paths.get(properties.rootDirectory()).resolve("temp").toAbsolutePath().normalize();
        try {
            Files.createDirectories(tempRootPath);
        } catch (IOException e) {
            throw new RuntimeException("Failed to initialize temporary storage root: " + tempRootPath, e);
        }
    }

    @Override
    public String getProviderId() {
        return "LOCAL_TEMP";
    }

    @Override
    public void storeChunk(UUID sessionId, int chunkNumber, InputStream inputStream, long sizeBytes) throws IOException {
        Path sessionDir = tempRootPath.resolve(sessionId.toString()).toAbsolutePath().normalize();
        if (!sessionDir.startsWith(tempRootPath)) {
            throw new SecurityException("Access outside temp directory is denied");
        }
        Files.createDirectories(sessionDir);

        Path chunkPath = sessionDir.resolve(String.valueOf(chunkNumber)).toAbsolutePath().normalize();
        if (!chunkPath.startsWith(sessionDir)) {
            throw new SecurityException("Access outside session directory is denied");
        }

        try (OutputStream out = Files.newOutputStream(chunkPath)) {
            byte[] buffer = new byte[8192];
            int read;
            while ((read = inputStream.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
        }
    }

    @Override
    public TemporaryResource retrieveChunk(UUID sessionId, int chunkNumber) throws IOException {
        Path sessionDir = tempRootPath.resolve(sessionId.toString()).toAbsolutePath().normalize();
        Path chunkPath = sessionDir.resolve(String.valueOf(chunkNumber)).toAbsolutePath().normalize();

        if (!chunkPath.startsWith(sessionDir) || !Files.exists(chunkPath)) {
            throw new FileNotFoundException("Chunk " + chunkNumber + " not found for session " + sessionId);
        }

        long size = Files.size(chunkPath);
        InputStream in = Files.newInputStream(chunkPath);
        return new TemporaryResource(in, size);
    }

    @Override
    public void deleteSessionData(UUID sessionId) throws IOException {
        Path sessionDir = tempRootPath.resolve(sessionId.toString()).toAbsolutePath().normalize();
        if (sessionDir.startsWith(tempRootPath) && Files.exists(sessionDir)) {
            try (var stream = Files.walk(sessionDir)) {
                stream.sorted((p1, p2) -> p2.compareTo(p1))
                      .forEach(p -> {
                          try {
                              Files.delete(p);
                          } catch (IOException e) {
                              // ignore or log
                          }
                      });
            }
        }
    }
}
