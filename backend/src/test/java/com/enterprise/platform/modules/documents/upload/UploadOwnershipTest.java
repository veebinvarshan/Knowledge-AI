package com.enterprise.platform.modules.documents.upload;

import com.enterprise.platform.core.config.properties.UploadProperties;
import com.enterprise.platform.modules.documents.upload.domain.UploadSession;
import com.enterprise.platform.modules.documents.upload.exception.UploadSessionOwnershipException;
import com.enterprise.platform.modules.documents.upload.repository.UploadSessionRepository;
import com.enterprise.platform.modules.documents.upload.service.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;

import java.io.ByteArrayInputStream;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class UploadOwnershipTest {

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
    void testTenantCrossBoundaryFails() {
        // GIVEN
        UUID sessionId = UUID.randomUUID();
        UUID ownerId = UUID.randomUUID();
        UploadSession session = new UploadSession("tenant-owner", ownerId, "doc.txt", 100L, "text/plain", 1, Instant.now().plusSeconds(30));

        when(sessionRepository.findById(sessionId)).thenReturn(Optional.of(session));

        // WHEN / THEN (Throws ownership error when requesting from tenant-attacker)
        assertThrows(UploadSessionOwnershipException.class, () ->
                uploadPipelineService.uploadChunk(sessionId, "tenant-attacker", ownerId, 1, new ByteArrayInputStream(new byte[0]), 10L, "hash")
        );
    }

    @Test
    void testUserCrossBoundaryFails() {
        // GIVEN
        UUID sessionId = UUID.randomUUID();
        UUID ownerId = UUID.randomUUID();
        UUID attackerId = UUID.randomUUID();
        UploadSession session = new UploadSession("tenant-1", ownerId, "doc.txt", 100L, "text/plain", 1, Instant.now().plusSeconds(30));

        when(sessionRepository.findById(sessionId)).thenReturn(Optional.of(session));

        // WHEN / THEN (Throws ownership error when requesting from attackerId)
        assertThrows(UploadSessionOwnershipException.class, () ->
                uploadPipelineService.uploadChunk(sessionId, "tenant-1", attackerId, 1, new ByteArrayInputStream(new byte[0]), 10L, "hash")
        );
    }
}
