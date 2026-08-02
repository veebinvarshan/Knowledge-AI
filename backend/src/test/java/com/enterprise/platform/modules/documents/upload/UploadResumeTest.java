package com.enterprise.platform.modules.documents.upload;

import com.enterprise.platform.core.config.properties.UploadProperties;
import com.enterprise.platform.modules.documents.upload.domain.UploadSession;
import com.enterprise.platform.modules.documents.upload.domain.UploadSessionStatus;
import com.enterprise.platform.modules.documents.upload.repository.UploadSessionRepository;
import com.enterprise.platform.modules.documents.upload.service.*;
import com.enterprise.platform.modules.documents.upload.service.dto.ResumeStatusDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class UploadResumeTest {

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
    void testResumeQueryReturnsUploadedChunksList() throws Exception {
        // GIVEN
        UUID sessionId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UploadSession session = new UploadSession("tenant-1", userId, "doc.pdf", 500L, "application/pdf", 3, Instant.now().plusSeconds(30));
        session.setStatus(UploadSessionStatus.UPLOADING);
        session.addChunk(1, 200L, "hash1", "SHA256");
        session.addChunk(3, 100L, "hash3", "SHA256");

        when(sessionRepository.findById(sessionId)).thenReturn(Optional.of(session));

        // WHEN
        ResumeStatusDto status = uploadPipelineService.getResumeStatus(sessionId, "tenant-1", userId);

        // THEN
        assertNotNull(status);
        assertEquals(3, status.chunksTotal());
        assertEquals(2, status.uploadedChunks().size());
        assertTrue(status.uploadedChunks().contains(1));
        assertTrue(status.uploadedChunks().contains(3));
        assertFalse(status.uploadedChunks().contains(2)); // missing chunk 2
    }
}
