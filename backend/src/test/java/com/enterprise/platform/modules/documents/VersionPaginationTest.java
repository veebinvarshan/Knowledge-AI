package com.enterprise.platform.modules.documents;

import com.enterprise.platform.modules.documents.domain.*;
import com.enterprise.platform.modules.documents.repository.*;
import com.enterprise.platform.modules.documents.service.DocumentServiceImpl;
import com.enterprise.platform.modules.documents.service.dto.VersionHistoryDto;
import com.enterprise.platform.modules.storage.repository.StorageObjectRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class VersionPaginationTest {

    private DocumentRepository documentRepository;
    private DocumentVersionRepository documentVersionRepository;
    private DocumentServiceImpl documentService;

    @BeforeEach
    void setUp() {
        documentRepository = mock(DocumentRepository.class);
        documentVersionRepository = mock(DocumentVersionRepository.class);
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
                mock(StorageObjectRepository.class),
                mock(com.enterprise.platform.modules.storage.service.StorageService.class),
                mock(com.enterprise.platform.modules.documents.service.QuarantineGuard.class)
        );
    }

    @Test
    void testVersionPaginationParameters() {
        // GIVEN
        UUID docId = UUID.randomUUID();
        Document doc = new Document("doc-1", "doc-1", null, "tenant-1", UUID.randomUUID(), UUID.randomUUID());
        DocumentVersion v1 = new DocumentVersion(doc, 1, UUID.randomUUID(), "hash", "SHA256", 100L, "text/plain", UUID.randomUUID(), VersionType.INITIAL, "");

        Page<DocumentVersion> page = new PageImpl<>(List.of(v1), PageRequest.of(0, 10), 1L);

        when(documentRepository.findById(docId)).thenReturn(Optional.of(doc));
        when(documentVersionRepository.findAllByDocumentId(eq(docId), any(Pageable.class))).thenReturn(page);

        // WHEN
        VersionHistoryDto history = documentService.getVersionHistory(docId, "tenant-1", UUID.randomUUID(), 0, 10);

        // THEN
        assertNotNull(history);
        assertEquals(1, history.items().size());
        assertEquals(0, history.page());
        assertEquals(10, history.size());
        assertEquals(1, history.totalElements());
    }
}
