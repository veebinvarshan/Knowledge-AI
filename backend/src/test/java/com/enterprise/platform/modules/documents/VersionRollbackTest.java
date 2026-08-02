package com.enterprise.platform.modules.documents;

import com.enterprise.platform.modules.documents.domain.*;
import com.enterprise.platform.modules.documents.repository.*;
import com.enterprise.platform.modules.documents.service.DocumentServiceImpl;
import com.enterprise.platform.modules.storage.domain.StorageObject;
import com.enterprise.platform.modules.storage.repository.StorageObjectRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class VersionRollbackTest {

    private DocumentRepository documentRepository;
    private DocumentVersionRepository documentVersionRepository;
    private StorageObjectRepository storageObjectRepository;
    private DocumentServiceImpl documentService;

    @BeforeEach
    void setUp() {
        documentRepository = mock(DocumentRepository.class);
        documentVersionRepository = mock(DocumentVersionRepository.class);
        storageObjectRepository = mock(StorageObjectRepository.class);
        ApplicationEventPublisher eventPublisher = mock(ApplicationEventPublisher.class);

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
    void testExceptionDuringPersistTriggersRollback() {
        // GIVEN
        UUID docId = UUID.randomUUID();
        UUID storageObjectId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        Document doc = new Document("doc-1", "doc-1", null, "tenant-1", UUID.randomUUID(), userId);
        StorageObject storageObject = new StorageObject("tenant-1/hello.pdf", "hello-key", "LOCAL", "checksum123", "SHA256", 1024L, "application/pdf");

        when(documentRepository.findById(docId)).thenReturn(Optional.of(doc));
        when(storageObjectRepository.findById(storageObjectId)).thenReturn(Optional.of(storageObject));
        // Throw exception when repository attempts to save Document aggregate
        when(documentRepository.save(any(Document.class))).thenThrow(new RuntimeException("Database connection failure"));

        // WHEN / THEN (Throws persistent exception, rolling back the JTA transaction)
        assertThrows(RuntimeException.class, () ->
                documentService.createVersion(docId, storageObjectId, userId, "tenant-1", VersionType.USER_UPLOAD, "new upload")
        );
    }
}
