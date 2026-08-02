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
import org.springframework.orm.ObjectOptimisticLockingFailureException;

import java.io.ByteArrayInputStream;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ConcurrentFinalizeTest {

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
    void testConcurrentFinalizeBlockedByOptimisticLocking() throws Exception {
        // GIVEN
        UUID sessionId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UploadSession session = new UploadSession("tenant-1", userId, "doc.pdf", 100L, "application/pdf", 1, Instant.now().plusSeconds(30));
        session.setStatus(UploadSessionStatus.UPLOADING);
        session.addChunk(1, 100L, "checksum", "SHA256");

        // First thread fetch session
        when(sessionRepository.findById(sessionId)).thenReturn(Optional.of(session));
        
        // Mock temporary chunk retrieval
        TemporaryResource tempRes = new TemporaryResource(new ByteArrayInputStream("data".getBytes()), 4);
        when(temporaryStorageService.retrieveChunk(eq(sessionId), anyInt())).thenReturn(tempRes);

        // When saving completed session, throw ObjectOptimisticLockingFailureException to simulate locking conflict
        when(sessionRepository.save(any(UploadSession.class))).thenThrow(new ObjectOptimisticLockingFailureException(UploadSession.class, sessionId));

        // WHEN / THEN (Finalize throws optimistic lock exception on db update)
        assertThrows(ObjectOptimisticLockingFailureException.class, () ->
                uploadPipelineService.finalizeUpload(sessionId, "tenant-1", userId, null)
        );
    }
}
