package com.enterprise.platform.modules.documents;

import com.enterprise.platform.modules.documents.domain.*;
import com.enterprise.platform.modules.documents.repository.*;
import com.enterprise.platform.modules.documents.service.DocumentServiceImpl;
import com.enterprise.platform.modules.storage.domain.StorageObject;
import com.enterprise.platform.modules.storage.repository.StorageObjectRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.context.ApplicationEventPublisher;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class EventOrderingTest {

    private DocumentRepository documentRepository;
    private DocumentVersionRepository documentVersionRepository;
    private StorageObjectRepository storageObjectRepository;
    private ApplicationEventPublisher eventPublisher;
    private DocumentServiceImpl documentService;

    @BeforeEach
    void setUp() {
        documentRepository = mock(DocumentRepository.class);
        documentVersionRepository = mock(DocumentVersionRepository.class);
        storageObjectRepository = mock(StorageObjectRepository.class);
        eventPublisher = mock(ApplicationEventPublisher.class);

        documentService = new DocumentServiceImpl(
                documentRepository,
                mock(FolderRepository.class),
                mock(com.enterprise.platform.modules.documents.service.SlugService.class),
                mock(com.enterprise.platform.modules.documents.service.DocumentLifecycleService.class),
                mock(com.enterprise.platform.modules.documents.service.OwnershipService.class),
                mock(com.enterprise.platform.modules.documents.service.MetadataService.class),
                mock(com.enterprise.platform.modules.documents.service.TagService.class),
                eventPublisher,
                documentVersionRepository,
                storageObjectRepository,
                mock(com.enterprise.platform.modules.storage.service.StorageService.class),
                mock(com.enterprise.platform.modules.documents.service.QuarantineGuard.class)
        );
    }

    @Test
    void testVersionCreatedEventFired() {
        // GIVEN
        UUID docId = UUID.randomUUID();
        UUID storageObjectId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        Document doc = new Document("doc-1", "doc-1", null, "tenant-1", UUID.randomUUID(), userId);
        StorageObject storageObject = new StorageObject("tenant-1/hello.pdf", "hello-key", "LOCAL", "checksum123", "SHA256", 1024L, "application/pdf");

        when(documentRepository.findById(docId)).thenReturn(Optional.of(doc));
        when(storageObjectRepository.findById(storageObjectId)).thenReturn(Optional.of(storageObject));

        // WHEN
        documentService.createVersion(docId, storageObjectId, userId, "tenant-1", VersionType.USER_UPLOAD, "new upload");

        // THEN
        ArgumentCaptor<Object> eventCaptor = ArgumentCaptor.forClass(Object.class);
        verify(eventPublisher, atLeastOnce()).publishEvent(eventCaptor.capture());

        boolean eventFound = eventCaptor.getAllValues().stream()
                .anyMatch(ev -> ev instanceof DocumentVersionEvents.DocumentVersionCreatedEvent);
        assertTrue(eventFound);
    }
}
