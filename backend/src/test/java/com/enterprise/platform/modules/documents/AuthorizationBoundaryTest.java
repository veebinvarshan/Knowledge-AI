package com.enterprise.platform.modules.documents;

import com.enterprise.platform.modules.documents.domain.Document;
import com.enterprise.platform.modules.documents.domain.DocumentVersion;
import com.enterprise.platform.modules.documents.domain.VersionType;
import com.enterprise.platform.modules.documents.repository.DocumentRepository;
import com.enterprise.platform.modules.documents.repository.DocumentVersionRepository;
import com.enterprise.platform.modules.documents.service.DocumentServiceImpl;
import com.enterprise.platform.modules.documents.service.dto.DocumentVersionDto;
import com.enterprise.platform.modules.documents.service.dto.VersionHistoryDto;
import com.enterprise.platform.modules.storage.domain.StorageObject;
import com.enterprise.platform.modules.storage.repository.StorageObjectRepository;
import com.enterprise.platform.core.exception.ForbiddenException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class AuthorizationBoundaryTest {

    private DocumentRepository documentRepository;
    private DocumentVersionRepository documentVersionRepository;
    private StorageObjectRepository storageObjectRepository;
    private com.enterprise.platform.modules.storage.service.StorageService storageService;
    private DocumentServiceImpl documentService;

    @BeforeEach
    void setUp() {
        documentRepository = mock(DocumentRepository.class);
        documentVersionRepository = mock(DocumentVersionRepository.class);
        storageObjectRepository = mock(StorageObjectRepository.class);
        storageService = mock(com.enterprise.platform.modules.storage.service.StorageService.class);
        ApplicationEventPublisher eventPublisher = mock(ApplicationEventPublisher.class);

        documentService = new DocumentServiceImpl(
                documentRepository,
                mock(com.enterprise.platform.modules.documents.repository.FolderRepository.class),
                mock(com.enterprise.platform.modules.documents.service.SlugService.class),
                mock(com.enterprise.platform.modules.documents.service.DocumentLifecycleService.class),
                mock(com.enterprise.platform.modules.documents.service.OwnershipService.class),
                mock(com.enterprise.platform.modules.documents.service.MetadataService.class),
                mock(com.enterprise.platform.modules.documents.service.TagService.class),
                eventPublisher,
                documentVersionRepository,
                storageObjectRepository,
                storageService,
                mock(com.enterprise.platform.modules.documents.service.QuarantineGuard.class)
        );
    }

    @Test
    void testTenantCrossBoundaryFails() {
        // GIVEN
        UUID docId = UUID.randomUUID();
        Document doc = new Document("doc-1", "doc-1", null, "tenant-1", UUID.randomUUID(), UUID.randomUUID());
        when(documentRepository.findById(docId)).thenReturn(Optional.of(doc));

        // WHEN / THEN (Querying via tenant-2 should fail)
        assertThrows(ForbiddenException.class, () ->
                documentService.getLatestVersion(docId, "tenant-2", UUID.randomUUID())
        );

        assertThrows(ForbiddenException.class, () ->
                documentService.getVersion(docId, 1, "tenant-2", UUID.randomUUID())
        );

        assertThrows(ForbiddenException.class, () ->
                documentService.getVersionHistory(docId, "tenant-2", UUID.randomUUID(), 0, 10)
        );

        assertThrows(ForbiddenException.class, () ->
                documentService.downloadVersion(docId, 1, "tenant-2", UUID.randomUUID())
        );
    }
}
