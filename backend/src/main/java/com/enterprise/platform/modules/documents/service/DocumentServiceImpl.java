package com.enterprise.platform.modules.documents.service;

import com.enterprise.platform.modules.documents.domain.*;
import com.enterprise.platform.modules.documents.domain.DocumentEvents.*;
import com.enterprise.platform.modules.documents.domain.DocumentVersionEvents.*;
import com.enterprise.platform.modules.documents.repository.*;
import com.enterprise.platform.modules.documents.service.dto.*;
import com.enterprise.platform.modules.storage.domain.StorageObject;
import com.enterprise.platform.modules.storage.repository.StorageObjectRepository;
import com.enterprise.platform.modules.storage.service.StorageService;
import com.enterprise.platform.modules.storage.service.dto.StorageResource;
import com.enterprise.platform.core.exception.*;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.*;

import com.enterprise.platform.modules.documents.service.DuplicateDetectionService;
import com.enterprise.platform.modules.documents.service.dto.DuplicateDetectionResult;

@Service
@Transactional
public class DocumentServiceImpl implements DocumentService {

    private final DocumentRepository documentRepository;
    private final FolderRepository folderRepository;
    private final SlugService slugService;
    private final DocumentLifecycleService lifecycleService;
    private final OwnershipService ownershipService;
    private final MetadataService metadataService;
    private final TagService tagService;
    private final ApplicationEventPublisher eventPublisher;
    private final DocumentVersionRepository documentVersionRepository;
    private final StorageObjectRepository storageObjectRepository;
    private final StorageService storageService;
    private final QuarantineGuard quarantineGuard;
    private final DuplicateDetectionService duplicateDetectionService;

    @Deprecated
    public DocumentServiceImpl(
            DocumentRepository documentRepository,
            FolderRepository folderRepository,
            SlugService slugService,
            DocumentLifecycleService lifecycleService,
            OwnershipService ownershipService,
            MetadataService metadataService,
            TagService tagService,
            ApplicationEventPublisher eventPublisher,
            DocumentVersionRepository documentVersionRepository,
            StorageObjectRepository storageObjectRepository,
            StorageService storageService,
            QuarantineGuard quarantineGuard) {
        this(documentRepository, folderRepository, slugService, lifecycleService, ownershipService, metadataService,
             tagService, eventPublisher, documentVersionRepository, storageObjectRepository, storageService, quarantineGuard,
             (tenantId, checksum, fileName, userId) -> new com.enterprise.platform.modules.documents.service.dto.DuplicateDetectionResult(
                     false, com.enterprise.platform.modules.documents.domain.DuplicateDetectionStrategy.ALLOW_DUPLICATE,
                     null, null, checksum, "Bypass duplicate check", "PROCEED"
             ));
    }

    @org.springframework.beans.factory.annotation.Autowired
    public DocumentServiceImpl(
            DocumentRepository documentRepository,
            FolderRepository folderRepository,
            SlugService slugService,
            DocumentLifecycleService lifecycleService,
            OwnershipService ownershipService,
            MetadataService metadataService,
            TagService tagService,
            ApplicationEventPublisher eventPublisher,
            DocumentVersionRepository documentVersionRepository,
            StorageObjectRepository storageObjectRepository,
            StorageService storageService,
            QuarantineGuard quarantineGuard,
            DuplicateDetectionService duplicateDetectionService) {
        this.documentRepository = documentRepository;
        this.folderRepository = folderRepository;
        this.slugService = slugService;
        this.lifecycleService = lifecycleService;
        this.ownershipService = ownershipService;
        this.metadataService = metadataService;
        this.tagService = tagService;
        this.eventPublisher = eventPublisher;
        this.documentVersionRepository = documentVersionRepository;
        this.storageObjectRepository = storageObjectRepository;
        this.storageService = storageService;
        this.quarantineGuard = quarantineGuard;
        this.duplicateDetectionService = duplicateDetectionService;
    }

    @Override
    public Document createDocument(String title, UUID folderId, String tenantId, UUID workspaceId, UUID ownerId) {
        // Validate folder if specified
        if (folderId != null) {
            Folder folder = folderRepository.findById(folderId)
                    .filter(f -> f.getTenantId().equals(tenantId) && f.getDeletedAt() == null)
                    .orElseThrow(() -> new IllegalArgumentException("Parent folder not found or belongs to a different tenant"));
        }

        // Generate unique slug
        String slug = slugService.generateUniqueSlug(title, tenantId, null);

        Document doc = new Document(title, slug, folderId, tenantId, workspaceId, ownerId);
        doc = documentRepository.save(doc);

        publishAfterCommit(new DocumentCreatedEvent(doc.getId(), tenantId, doc.getTitle(), ownerId));
        return doc;
    }

    @Override
    public Document renameDocument(UUID id, String newTitle, String tenantId, UUID userId) {
        Document doc = getDocumentOrThrow(id, tenantId);
        String oldTitle = doc.getTitle();

        // Calculate and validate new slug
        String newSlug = slugService.generateUniqueSlug(newTitle, tenantId, id);
        doc.rename(newTitle, newSlug, userId);
        doc = documentRepository.save(doc);

        publishAfterCommit(new DocumentRenamedEvent(id, tenantId, oldTitle, newTitle));
        return doc;
    }

    @Override
    public Document moveDocument(UUID id, UUID targetFolderId, String tenantId, UUID userId) {
        Document doc = getDocumentOrThrow(id, tenantId);
        UUID oldFolderId = doc.getFolderId();

        if (Objects.equals(oldFolderId, targetFolderId)) {
            return doc;
        }

        if (targetFolderId != null) {
            folderRepository.findById(targetFolderId)
                    .filter(f -> f.getTenantId().equals(tenantId) && f.getDeletedAt() == null)
                    .orElseThrow(() -> new IllegalArgumentException("Target folder not found or belongs to a different tenant"));
        }

        doc.move(targetFolderId, userId);
        doc = documentRepository.save(doc);

        publishAfterCommit(new DocumentMovedEvent(id, tenantId, oldFolderId, targetFolderId));
        return doc;
    }

    @Override
    public void archiveDocument(UUID id, String tenantId, UUID userId) {
        Document doc = getDocumentOrThrow(id, tenantId);
        lifecycleService.archive(doc, userId);
        documentRepository.save(doc);

        publishAfterCommit(new DocumentArchivedEvent(id, tenantId));
    }

    @Override
    public Document restoreDocument(UUID id, String tenantId, UUID userId) {
        // Bypass soft-delete check to fetch deleted document
        Document doc = documentRepository.findById(id)
                .filter(d -> d.getTenantId().equals(tenantId))
                .orElseThrow(() -> new NoSuchElementException("Document not found"));

        lifecycleService.restore(doc, userId);
        doc = documentRepository.save(doc);

        publishAfterCommit(new DocumentRestoredEvent(id, tenantId));
        return doc;
    }

    @Override
    public void deleteDocument(UUID id, String tenantId, UUID userId) {
        Document doc = getDocumentOrThrow(id, tenantId);
        lifecycleService.softDelete(doc, userId);
        documentRepository.save(doc);

        publishAfterCommit(new DocumentDeletedEvent(id, tenantId));
    }

    @Override
    public Document updateMetadata(UUID id, Map<String, Object> metadata, String tenantId, UUID userId) {
        Document doc = getDocumentOrThrow(id, tenantId);
        metadataService.updateMetadata(doc, metadata, userId);
        doc = documentRepository.save(doc);

        publishAfterCommit(new DocumentMetadataUpdatedEvent(id, tenantId, metadata.keySet()));
        return doc;
    }

    @Override
    public Document changeOwnership(UUID id, UUID newOwnerId, String tenantId, UUID userId) {
        Document doc = getDocumentOrThrow(id, tenantId);
        UUID oldOwnerId = doc.getOwnerId();

        ownershipService.changeOwnership(doc, newOwnerId, userId);
        doc = documentRepository.save(doc);

        publishAfterCommit(new DocumentOwnershipChangedEvent(id, tenantId, oldOwnerId, newOwnerId));
        return doc;
    }

    @Override
    public Document assignTags(UUID id, Set<UUID> tagIds, String tenantId, UUID userId) {
        Document doc = getDocumentOrThrow(id, tenantId);
        tagService.assignTags(doc, tagIds, userId);
        doc = documentRepository.save(doc);

        publishAfterCommit(new DocumentTagAssignedEvent(id, tenantId, tagIds));
        return doc;
    }

    @Override
    public Document removeTags(UUID id, Set<UUID> tagIds, String tenantId, UUID userId) {
        Document doc = getDocumentOrThrow(id, tenantId);
        tagService.removeTags(doc, tagIds, userId);
        doc = documentRepository.save(doc);

        publishAfterCommit(new DocumentTagRemovedEvent(id, tenantId, tagIds));
        return doc;
    }

    @Override
    @Transactional(readOnly = true)
    public Document getDocumentOrThrow(UUID id, String tenantId) {
        return documentRepository.findById(id)
                .filter(d -> d.getTenantId().equals(tenantId) && d.getDeletedAt() == null)
                .orElseThrow(() -> new NoSuchElementException("Document not found"));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Document> getDocuments(UUID folderId, String tenantId, Pageable pageable) {
        if (folderId == null) {
            return documentRepository.findRootDocuments(tenantId, pageable);
        }
        return documentRepository.findByTenantIdAndFolderIdAndDeletedAtIsNull(tenantId, folderId, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Document> searchByMetadata(String key, String value, String tenantId, Pageable pageable) {
        return documentRepository.findByMetadataKeyValue(tenantId, key, value, pageable);
    }

    @Override
    public LightweightVersionDto createVersion(UUID documentId, UUID storageObjectId, UUID userId, String tenantId, VersionType versionType, String comment) {
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found"));

        if (!document.getTenantId().equals(tenantId)) {
            throw new ForbiddenException("Access denied: tenant mismatch");
        }
        if (document.getDeletedAt() != null) {
            throw new BadRequestException("Cannot add version to a deleted document");
        }

        StorageObject storageObject = storageObjectRepository.findById(storageObjectId)
                .orElseThrow(() -> new ResourceNotFoundException("StorageObject not found"));

        if (storageObject.getLogicalPath() != null && !storageObject.getLogicalPath().contains(tenantId)) {
            throw new ForbiddenException("Access denied: StorageObject tenant mismatch");
        }

        // Duplicate Check
        DuplicateDetectionResult duplicateResult = duplicateDetectionService.evaluateDuplicate(tenantId, storageObject.getChecksum(), storageObject.getLogicalPath(), userId);
        if (duplicateResult.duplicateFound() && duplicateResult.recommendedAction().equals("REJECT")) {
            throw new com.enterprise.platform.modules.documents.exception.DuplicateDocumentException(duplicateResult.reason());
        }

        UUID oldActiveVersionId = document.getCurrentVersionId();
        DocumentVersion oldActiveVersion = null;
        if (oldActiveVersionId != null) {
            oldActiveVersion = documentVersionRepository.findById(oldActiveVersionId).orElse(null);
        }

        DocumentVersion version = document.addVersion(
                storageObjectId,
                storageObject.getChecksum(),
                storageObject.getChecksumAlgorithm(),
                storageObject.getSizeBytes(),
                storageObject.getMimeType(),
                userId,
                versionType,
                comment
        );

        String logicalPath = storageObject.getLogicalPath();
        if (logicalPath != null) {
            String filename = logicalPath.substring(logicalPath.lastIndexOf('/') + 1);
            String ext = filename.contains(".") ? filename.substring(filename.lastIndexOf('.') + 1) : "";
            version.setOriginalFileName(filename);
            version.setExtension(ext);
        }

        documentRepository.save(document);

        publishAfterCommit(new DocumentVersionCreatedEvent(
                documentId,
                version.getId(),
                version.getVersionNumber(),
                storageObjectId,
                version.getChecksum(),
                tenantId,
                userId
        ));

        if (oldActiveVersion != null) {
            publishAfterCommit(new DocumentVersionSupersededEvent(
                    documentId,
                    oldActiveVersion.getId(),
                    oldActiveVersion.getVersionNumber(),
                    oldActiveVersion.getStorageObjectId(),
                    oldActiveVersion.getChecksum(),
                    tenantId,
                    userId
            ));
        }

        return toLightweightDto(version, document);
    }

    @Override
    @Transactional(readOnly = true)
    public LightweightVersionDto getLatestVersion(UUID documentId, String tenantId, UUID userId) {
        Document document = getDocumentAndVerifyAccess(documentId, tenantId, userId);
        UUID currentVersionId = document.getCurrentVersionId();
        if (currentVersionId == null) {
            throw new ResourceNotFoundException("No active version found for document");
        }
        DocumentVersion version = documentVersionRepository.findById(currentVersionId)
                .orElseThrow(() -> new ResourceNotFoundException("Document version not found"));
        return toLightweightDto(version, document);
    }

    @Override
    @Transactional(readOnly = true)
    public LightweightVersionDto getVersion(UUID documentId, int versionNumber, String tenantId, UUID userId) {
        Document document = getDocumentAndVerifyAccess(documentId, tenantId, userId);
        DocumentVersion version = documentVersionRepository.findByDocumentIdAndVersionNumber(documentId, versionNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Document version not found"));
        return toLightweightDto(version, document);
    }

    @Override
    @Transactional(readOnly = true)
    public VersionHistoryDto getVersionHistory(UUID documentId, String tenantId, UUID userId, int page, int size) {
        Document document = getDocumentAndVerifyAccess(documentId, tenantId, userId);
        Page<DocumentVersion> versionsPage = documentVersionRepository.findAllByDocumentId(
                documentId,
                PageRequest.of(page, size, Sort.by(Sort.Order.desc("versionNumber")))
        );
        List<LightweightVersionDto> items = versionsPage.getContent().stream()
                .map(v -> toLightweightDto(v, document))
                .toList();
        return new VersionHistoryDto(items, page, size, versionsPage.getTotalElements(), versionsPage.getTotalPages());
    }

    @Override
    @Transactional(readOnly = true)
    public StorageResource downloadVersion(UUID documentId, int versionNumber, String tenantId, UUID userId) {
        getDocumentAndVerifyAccess(documentId, tenantId, userId);
        DocumentVersion version = documentVersionRepository.findByDocumentIdAndVersionNumber(documentId, versionNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Document version not found"));

        if (quarantineGuard.isQuarantined(version.getId())) {
            throw new ForbiddenException("File has been quarantined due to security scan detection");
        }

        StorageObject storageObject = storageObjectRepository.findById(version.getStorageObjectId())
                .orElseThrow(() -> new ResourceNotFoundException("StorageObject not found"));

        return storageService.retrieve(storageObject.getLogicalPath());
    }

    private Document getDocumentAndVerifyAccess(UUID documentId, String tenantId, UUID userId) {
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found"));

        if (!document.getTenantId().equals(tenantId)) {
            throw new ForbiddenException("Access denied: tenant mismatch");
        }
        if (document.getDeletedAt() != null) {
            throw new BadRequestException("Document is deleted");
        }
        return document;
    }

    private LightweightVersionDto toLightweightDto(DocumentVersion v, Document doc) {
        boolean isCurrent = v.getId() != null && v.getId().equals(doc.getCurrentVersionId());
        String provider = storageObjectRepository.findById(v.getStorageObjectId())
                .map(so -> so.getProviderId())
                .orElse("UNKNOWN");
        return new LightweightVersionDto(
                v.getVersionNumber(),
                v.getOriginalFileName() != null ? v.getOriginalFileName() : doc.getTitle(),
                v.getMimeType(),
                v.getSizeBytes(),
                v.getChecksum(),
                v.getCreatedBy(),
                v.getCreatedAt(),
                isCurrent,
                v.getStatus().name(),
                provider
        );
    }

    private void publishAfterCommit(Object event) {
        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    eventPublisher.publishEvent(event);
                }
            });
        } else {
            eventPublisher.publishEvent(event);
        }
    }
}
