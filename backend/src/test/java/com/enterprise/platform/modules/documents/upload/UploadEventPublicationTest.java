package com.enterprise.platform.modules.documents.upload;

import com.enterprise.platform.core.config.properties.UploadProperties;
import com.enterprise.platform.modules.documents.upload.domain.UploadEvents;
import com.enterprise.platform.modules.documents.upload.repository.UploadSessionRepository;
import com.enterprise.platform.modules.documents.upload.domain.UploadSession;
import com.enterprise.platform.modules.documents.upload.service.*;
import com.enterprise.platform.modules.documents.upload.service.dto.UploadSessionInitDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.context.ApplicationEventPublisher;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class UploadEventPublicationTest {

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
    void testEventPublishedOnInitialization() throws Exception {
        // GIVEN
        UploadSessionInitDto init = new UploadSessionInitDto("doc.txt", 100L, "text/plain", 1);
        when(uploadProperties.maxFileSize()).thenReturn(1000L);
        when(uploadProperties.maxChunksPerUpload()).thenReturn(10);
        when(uploadProperties.maxConcurrentUploadsPerUser()).thenReturn(5);
        when(uploadProperties.maxConcurrentUploadsPerTenant()).thenReturn(20);

        // WHEN
        uploadPipelineService.initializeSession("tenant-1", UUID.randomUUID(), init);

        // THEN
        ArgumentCaptor<Object> eventCaptor = ArgumentCaptor.forClass(Object.class);
        verify(eventPublisher, times(1)).publishEvent(eventCaptor.capture());
        
        Object event = eventCaptor.getValue();
        assertTrue(event instanceof UploadEvents.UploadSessionInitializedEvent);
        
        UploadEvents.UploadSessionInitializedEvent initializedEvent = (UploadEvents.UploadSessionInitializedEvent) event;
        assertEquals(1, initializedEvent.getEventVersion());
        assertEquals("tenant-1", initializedEvent.getTenantId());
    }
}
