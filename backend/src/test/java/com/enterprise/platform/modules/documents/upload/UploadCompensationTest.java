package com.enterprise.platform.modules.documents.upload;

import com.enterprise.platform.core.config.properties.UploadProperties;
import com.enterprise.platform.modules.documents.upload.domain.UploadSession;
import com.enterprise.platform.modules.documents.upload.domain.UploadSessionStatus;
import com.enterprise.platform.modules.documents.upload.exception.UploadException;
import com.enterprise.platform.modules.documents.upload.repository.UploadSessionRepository;
import com.enterprise.platform.modules.documents.upload.service.*;
import com.enterprise.platform.modules.documents.upload.service.dto.TemporaryResource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class UploadCompensationTest {

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

        when(sessionRepository.save(any(UploadSession.class))).thenAnswer(invocation -> {
            UploadSession s = invocation.getArgument(0);
            s.setId(UUID.randomUUID());
            return s;
        });

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
    void testCompensationTriggeredOnFinalizeException() throws Exception {
        // GIVEN
        UUID sessionId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UploadSession session = new UploadSession("tenant-1", userId, "doc.txt", 100L, "text/plain", 1, Instant.now().plusSeconds(30));
        session.setStatus(UploadSessionStatus.UPLOADING);
        session.addChunk(1, 100L, "checksum", "SHA256");

        when(sessionRepository.findById(sessionId)).thenReturn(Optional.of(session));
        // Stub retrieveChunk to return valid TemporaryResource
        TemporaryResource tempRes = new TemporaryResource(new ByteArrayInputStream("data".getBytes()), 4);
        when(temporaryStorageService.retrieveChunk(eq(sessionId), anyInt())).thenReturn(tempRes);
        // Throw exception during final storage store operation
        when(storageService.store(any(), anyString(), anyString())).thenThrow(new RuntimeException("Storage disk full"));

        // WHEN / THEN (Finalize fails and throws exception)
        assertThrows(UploadException.class, () ->
                uploadPipelineService.finalizeUpload(sessionId, "tenant-1", userId, "checksum")
        );

        // THEN (Compensation deletes temporary session directory)
        verify(temporaryStorageService, times(1)).deleteSessionData(sessionId);
        assertEquals(UploadSessionStatus.FAILED, session.getStatus());
    }
}
