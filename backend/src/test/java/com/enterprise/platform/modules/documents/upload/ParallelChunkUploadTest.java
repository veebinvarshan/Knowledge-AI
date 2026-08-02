package com.enterprise.platform.modules.documents.upload;

import com.enterprise.platform.core.config.properties.UploadProperties;
import com.enterprise.platform.modules.documents.upload.domain.UploadSession;
import com.enterprise.platform.modules.documents.upload.domain.UploadSessionStatus;
import com.enterprise.platform.modules.documents.upload.repository.UploadSessionRepository;
import com.enterprise.platform.modules.documents.upload.service.*;
import com.enterprise.platform.modules.documents.upload.service.dto.TemporaryResource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ParallelChunkUploadTest {

    private UploadSessionRepository sessionRepository;
    private TemporaryStorageService temporaryStorageService;
    private com.enterprise.platform.modules.storage.service.StorageService storageService;
    private QuotaService quotaService;
    private UploadProperties uploadProperties;
    private ApplicationEventPublisher eventPublisher;
    private UploadPipelineServiceImpl uploadPipelineService;

    @BeforeEach
    void setUp() {
        sessionRepository = mock(UploadSessionRepository.class);
        temporaryStorageService = mock(TemporaryStorageService.class);
        storageService = mock(com.enterprise.platform.modules.storage.service.StorageService.class);
        quotaService = mock(QuotaService.class);
        uploadProperties = mock(UploadProperties.class);
        eventPublisher = mock(ApplicationEventPublisher.class);

        uploadPipelineService = new UploadPipelineServiceImpl(
                sessionRepository,
                temporaryStorageService,
                storageService,
                quotaService,
                uploadProperties,
                eventPublisher
        );
    }

    @Test
    void testOutofOrderChunkUploadSuccess() throws Exception {
        // GIVEN
        UUID sessionId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UploadSession session = new UploadSession("tenant-1", userId, "doc.pdf", 300L, "application/pdf", 3, Instant.now().plusSeconds(30));

        when(sessionRepository.findById(sessionId)).thenReturn(Optional.of(session));
        when(uploadProperties.maxChunkSize()).thenReturn(1000L);
        when(uploadProperties.minChunkSize()).thenReturn(1L);

        // Stub temporary store retrieving for checksum evaluation
        String hashHello = "2cf24dba5fb0a30e26e83b2ac5b9e29e1b161e5c1fa7425e73043362938b9824";
        when(temporaryStorageService.retrieveChunk(eq(sessionId), anyInt())).thenAnswer(inv -> 
                new TemporaryResource(new ByteArrayInputStream("hello".getBytes()), 5)
        );

        // WHEN (Upload chunk 3 first, then chunk 1)
        assertDoesNotThrow(() ->
                uploadPipelineService.uploadChunk(sessionId, "tenant-1", userId, 3, new ByteArrayInputStream("hello".getBytes()), 5L, hashHello)
        );

        assertDoesNotThrow(() ->
                uploadPipelineService.uploadChunk(sessionId, "tenant-1", userId, 1, new ByteArrayInputStream("hello".getBytes()), 5L, hashHello)
        );

        // THEN
        assertEquals(2, session.getChunks().size());
        assertTrue(session.getChunks().stream().anyMatch(c -> c.getChunkNumber() == 3));
        assertTrue(session.getChunks().stream().anyMatch(c -> c.getChunkNumber() == 1));
    }
}
