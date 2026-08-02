package com.enterprise.platform.modules.documents;

import com.enterprise.platform.modules.documents.domain.Document;
import com.enterprise.platform.modules.documents.domain.DocumentEvents.*;
import com.enterprise.platform.modules.documents.domain.Folder;
import com.enterprise.platform.modules.documents.domain.LifecycleStatus;
import com.enterprise.platform.modules.documents.repository.DocumentRepository;
import com.enterprise.platform.modules.documents.repository.FolderRepository;
import com.enterprise.platform.modules.documents.service.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class DocumentDomainTest {

    private DocumentRepository documentRepository;
    private FolderRepository folderRepository;
    private SlugService slugService;
    private DocumentLifecycleService lifecycleService;
    private OwnershipService ownershipService;
    private MetadataService metadataService;
    private TagService tagService;
    private ApplicationEventPublisher eventPublisher;
    private DocumentService documentService;

    private String tenantId;
    private UUID ownerId;
    private UUID workspaceId;

    @BeforeEach
    void setUp() {
        documentRepository = mock(DocumentRepository.class);
        folderRepository = mock(FolderRepository.class);
        
        slugService = new SlugServiceImpl(documentRepository);
        lifecycleService = new DocumentLifecycleServiceImpl();
        ownershipService = new OwnershipServiceImpl();
        metadataService = new MetadataServiceImpl();
        tagService = new TagServiceImpl();
        eventPublisher = mock(ApplicationEventPublisher.class);

        documentService = new DocumentServiceImpl(
                documentRepository,
                folderRepository,
                slugService,
                lifecycleService,
                ownershipService,
                metadataService,
                tagService,
                eventPublisher,
                mock(com.enterprise.platform.modules.documents.repository.DocumentVersionRepository.class),
                mock(com.enterprise.platform.modules.storage.repository.StorageObjectRepository.class),
                mock(com.enterprise.platform.modules.storage.service.StorageService.class),
                mock(com.enterprise.platform.modules.documents.service.QuarantineGuard.class)
        );

        tenantId = "acme-corp";
        ownerId = UUID.randomUUID();
        workspaceId = UUID.randomUUID();
    }

    @Test
    void testDocumentCreatedAndSlugGenerated() {
        // GIVEN
        String title = "Enterprise Proposal 2026";
        when(documentRepository.existsByTenantIdAndSlugAndDeletedAtIsNull(tenantId, "enterprise-proposal-2026"))
                .thenReturn(false);
        when(documentRepository.save(any(Document.class))).thenAnswer(invocation -> {
            Document d = invocation.getArgument(0);
            if (d.getId() == null) {
                d.setId(UUID.randomUUID());
            }
            return d;
        });

        // WHEN
        Document doc = documentService.createDocument(title, null, tenantId, workspaceId, ownerId);

        // THEN
        assertNotNull(doc);
        assertEquals("enterprise-proposal-2026", doc.getSlug());
        assertEquals(LifecycleStatus.DRAFT, doc.getStatus());
        assertEquals(title, doc.getTitle());
    }

    @Test
    void testSlugGenerationUniquenessSuffixIncrement() {
        // GIVEN
        String title = "Proposal";
        when(documentRepository.existsByTenantIdAndSlugAndDeletedAtIsNull(tenantId, "proposal")).thenReturn(true);
        when(documentRepository.existsByTenantIdAndSlugAndDeletedAtIsNull(tenantId, "proposal-1")).thenReturn(true);
        when(documentRepository.existsByTenantIdAndSlugAndDeletedAtIsNull(tenantId, "proposal-2")).thenReturn(false);

        // WHEN
        String slug = slugService.generateUniqueSlug(title, tenantId, null);

        // THEN
        assertEquals("proposal-2", slug);
    }

    @Test
    void testRenameDocumentRecalculatesSlug() {
        // GIVEN
        UUID docId = UUID.randomUUID();
        Document doc = new Document("Old title", "old-title", null, tenantId, workspaceId, ownerId);
        doc.setId(docId);

        when(documentRepository.findById(docId)).thenReturn(Optional.of(doc));
        when(documentRepository.existsByTenantIdAndSlugAndDeletedAtIsNullAndIdNot(tenantId, "new-title", docId))
                .thenReturn(false);
        when(documentRepository.save(any(Document.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // WHEN
        Document renamed = documentService.renameDocument(docId, "New Title", tenantId, ownerId);

        // THEN
        assertEquals("New Title", renamed.getTitle());
        assertEquals("new-title", renamed.getSlug());
    }

    @Test
    void testUpdateMetadataValidations() {
        // GIVEN
        UUID docId = UUID.randomUUID();
        Document doc = new Document("Title", "title", null, tenantId, workspaceId, ownerId);
        doc.setId(docId);
        when(documentRepository.findById(docId)).thenReturn(Optional.of(doc));
        when(documentRepository.save(any(Document.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Map<String, Object> meta = new HashMap<>();
        meta.put("department", "Sales");
        meta.put("pages-count", 24);

        // WHEN
        Document updated = documentService.updateMetadata(docId, meta, tenantId, ownerId);

        // THEN
        assertEquals(2, updated.getStructuredMetadata().size());
        assertEquals("Sales", updated.getStructuredMetadata().get("department"));
    }

    @Test
    void testUpdateMetadataInvalidKeysFails() {
        // GIVEN
        UUID docId = UUID.randomUUID();
        Document doc = new Document("Title", "title", null, tenantId, workspaceId, ownerId);
        doc.setId(docId);
        when(documentRepository.findById(docId)).thenReturn(Optional.of(doc));

        // Key with spaces
        Map<String, Object> metaWithSpaces = Map.of("invalid key", "value");
        assertThrows(IllegalArgumentException.class, () ->
            documentService.updateMetadata(docId, metaWithSpaces, tenantId, ownerId)
        );

        // Key with special characters
        Map<String, Object> metaWithSpecial = Map.of("invalidKey#", "value");
        assertThrows(IllegalArgumentException.class, () ->
            documentService.updateMetadata(docId, metaWithSpecial, tenantId, ownerId)
        );
    }

    @Test
    void testOwnershipTransferSuccess() {
        // GIVEN
        UUID docId = UUID.randomUUID();
        Document doc = new Document("Title", "title", null, tenantId, workspaceId, ownerId);
        doc.setId(docId);
        UUID newOwner = UUID.randomUUID();

        when(documentRepository.findById(docId)).thenReturn(Optional.of(doc));
        when(documentRepository.save(any(Document.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // WHEN
        Document updated = documentService.changeOwnership(docId, newOwner, tenantId, ownerId);

        // THEN
        assertEquals(newOwner, updated.getOwnerId());
    }

    @Test
    void testArchiveDocumentRestrictedToReadyState() {
        // GIVEN
        UUID docId = UUID.randomUUID();
        Document doc = new Document("Title", "title", null, tenantId, workspaceId, ownerId);
        doc.setId(docId);
        
        // Document starts as DRAFT
        assertEquals(LifecycleStatus.DRAFT, doc.getStatus());
        when(documentRepository.findById(docId)).thenReturn(Optional.of(doc));

        // WHEN / THEN (Archive fails on DRAFT status)
        assertThrows(IllegalStateException.class, () ->
            documentService.archiveDocument(docId, tenantId, ownerId)
        );

        // Transition to READY, archive succeeds
        doc.setStatus(LifecycleStatus.READY);
        documentService.archiveDocument(docId, tenantId, ownerId);
        assertEquals(LifecycleStatus.ARCHIVED, doc.getStatus());
    }

    @Test
    void testTenantBoundaryIsolationViolation() {
        // GIVEN
        UUID docId = UUID.randomUUID();
        Document doc = new Document("Secret", "secret", null, "other-corp", workspaceId, ownerId);
        doc.setId(docId);
        
        when(documentRepository.findById(docId)).thenReturn(Optional.of(doc));

        // WHEN / THEN (Access from acme-corp context fails)
        assertThrows(NoSuchElementException.class, () ->
            documentService.renameDocument(docId, "Hack", "acme-corp", ownerId)
        );
    }
}
