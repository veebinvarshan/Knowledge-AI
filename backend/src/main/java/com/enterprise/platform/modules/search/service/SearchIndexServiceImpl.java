package com.enterprise.platform.modules.search.service;

import com.enterprise.platform.core.config.properties.SearchProperties;
import com.enterprise.platform.core.config.properties.SearchWorkerProperties;
import com.enterprise.platform.modules.documents.domain.DocumentVersion;
import com.enterprise.platform.modules.documents.repository.DocumentVersionRepository;
import com.enterprise.platform.modules.metadata.domain.ExtractedMetadata;
import com.enterprise.platform.modules.metadata.repository.ExtractedMetadataRepository;
import com.enterprise.platform.modules.ocr.domain.OcrText;
import com.enterprise.platform.modules.ocr.repository.OcrTextRepository;
import com.enterprise.platform.modules.search.domain.*;
import com.enterprise.platform.modules.search.domain.SearchEvents.*;
import com.enterprise.platform.modules.search.indexing.SearchNormalizer;
import com.enterprise.platform.modules.search.provider.SearchProvider;
import com.enterprise.platform.modules.search.repository.SearchDocumentRepository;
import com.enterprise.platform.modules.search.repository.SearchJobRepository;
import com.enterprise.platform.modules.storage.domain.StorageObject;
import com.enterprise.platform.modules.storage.repository.StorageObjectRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadPoolExecutor;

@Service
@Transactional
public class SearchIndexServiceImpl implements SearchIndexService {

    private static final Logger log = LoggerFactory.getLogger(SearchIndexServiceImpl.class);

    private final SearchJobRepository jobRepository;
    private final SearchDocumentRepository documentRepository;
    private final OcrTextRepository ocrTextRepository;
    private final ExtractedMetadataRepository metadataRepository;
    private final StorageObjectRepository storageObjectRepository;
    private final DocumentVersionRepository documentVersionRepository;
    private final SearchProviderResolver providerResolver;
    private final SearchNormalizer normalizer;
    private final ApplicationEventPublisher eventPublisher;

    private final SearchProperties properties;
    private final ThreadPoolTaskExecutor taskExecutor;

    public SearchIndexServiceImpl(
            SearchJobRepository jobRepository,
            SearchDocumentRepository documentRepository,
            OcrTextRepository ocrTextRepository,
            ExtractedMetadataRepository metadataRepository,
            StorageObjectRepository storageObjectRepository,
            DocumentVersionRepository documentVersionRepository,
            SearchProviderResolver providerResolver,
            SearchNormalizer normalizer,
            ApplicationEventPublisher eventPublisher,
            SearchProperties properties,
            SearchWorkerProperties workerProperties) {
        this.jobRepository = jobRepository;
        this.documentRepository = documentRepository;
        this.ocrTextRepository = ocrTextRepository;
        this.metadataRepository = metadataRepository;
        this.storageObjectRepository = storageObjectRepository;
        this.documentVersionRepository = documentVersionRepository;
        this.providerResolver = providerResolver;
        this.normalizer = normalizer;
        this.eventPublisher = eventPublisher;
        this.properties = properties;

        // Bounded worker queue with CallerRunsPolicy backpressure
        this.taskExecutor = new ThreadPoolTaskExecutor();
        this.taskExecutor.setCorePoolSize(workerProperties.threads());
        this.taskExecutor.setMaxPoolSize(workerProperties.threads());
        this.taskExecutor.setQueueCapacity(workerProperties.queueCapacity());
        this.taskExecutor.setThreadNamePrefix("search-worker-");
        this.taskExecutor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        this.taskExecutor.initialize();
    }

    @Override
    public SearchJob submitIndexJob(String tenantId, UUID documentId, UUID versionId, String indexType) {
        if (!properties.enabled()) {
            log.info("Search indexing is disabled; bypassing job submission.");
            return null;
        }

        SearchIndexType type = SearchIndexType.valueOf(indexType.toUpperCase());
        SearchJob job = new SearchJob(documentId, versionId, tenantId, type);
        job = jobRepository.save(job);

        eventPublisher.publishEvent(new SearchIndexRequestedEvent(
                job.getJobId(), documentId, versionId, tenantId, indexType, properties.provider()
        ));

        // Submit task asynchronously
        final UUID jobId = job.getJobId();
        taskExecutor.submit(() -> executeIndexing(jobId, tenantId));

        return job;
    }

    @Override
    public void executeIndexing(UUID jobId, String tenantId) {
        SearchJob job = jobRepository.findById(jobId).orElse(null);
        if (job == null) return;

        try {
            job.transitionToIndexing();
            job = jobRepository.save(job);
        } catch (Exception e) {
            log.error("Failed to transition search job to indexing: {}", jobId, e);
            return;
        }

        eventPublisher.publishEvent(new SearchIndexStartedEvent(
                job.getJobId(), job.getDocumentId(), job.getVersionId(), tenantId, job.getIndexType().name(), properties.provider()
        ));

        long start = System.currentTimeMillis();
        try {
            // Retrieve OCR Text & Extracted Metadata snapshots
            OcrText ocrText = ocrTextRepository.findById(job.getVersionId()).orElse(null);
            ExtractedMetadata meta = metadataRepository.findById(job.getVersionId()).orElse(null);

            // Refinement 3: Construct Search text using precedence:
            // 1. OCR extracted text (if available)
            // 2. Native searchable text
            // 3. Metadata text
            // 4. Document title
            // 5. File name
            // 6. Tags
            // 7. Author
            String sourceText = "";
            String title = "";
            String filename = "";
            String mimeType = "";
            String language = "";
            String author = "";

            if (ocrText != null && ocrText.getExtractedText() != null && !ocrText.getExtractedText().isBlank()) {
                sourceText = ocrText.getExtractedText();
            }

            // Load DocumentVersion to resolve filename and storage mappings
            DocumentVersion version = documentVersionRepository.findById(job.getVersionId()).orElse(null);
            if (version != null) {
                filename = version.getOriginalFileName();
                mimeType = version.getMimeType();
            }

            if (meta != null) {
                title = meta.getTitle();
                author = meta.getAuthor();
                language = meta.getLanguage();

                if (sourceText.isBlank()) {
                    // Aggregate text fields if OCR was skipped or empty
                    StringBuilder sb = new StringBuilder();
                    if (title != null) sb.append(title).append(" ");
                    if (meta.getSubject() != null) sb.append(meta.getSubject()).append(" ");
                    if (author != null) sb.append(author).append(" ");
                    if (meta.getCompany() != null) sb.append(meta.getCompany()).append(" ");
                    if (meta.getKeywords() != null) sb.append(meta.getKeywords()).append(" ");
                    if (meta.getProducer() != null) sb.append(meta.getProducer()).append(" ");
                    if (meta.getCreator() != null) sb.append(meta.getCreator()).append(" ");
                    if (meta.getApplication() != null) sb.append(meta.getApplication()).append(" ");
                    sourceText = sb.toString().trim();
                }
                if (sourceText.isBlank()) {
                    sourceText = title + " " + filename + " " + author;
                }
            } else if (version != null) {
                // Fetch storage object details if metadata is missing
                StorageObject storageObject = storageObjectRepository.findById(version.getStorageObjectId()).orElse(null);
                if (storageObject != null) {
                    if (filename == null || filename.isBlank()) {
                        filename = storageObject.getLogicalPath();
                    }
                    if (sourceText.isBlank()) {
                        sourceText = filename;
                    }
                }
            }

            // Standard Normalization
            String normalizedText = normalizer.normalize(sourceText);

            // Build Immutable SearchDocument
            SearchDocument doc = SearchDocument.builder()
                    .documentId(job.getDocumentId())
                    .versionId(job.getVersionId())
                    .tenantId(tenantId)
                    .title(title)
                    .filename(filename)
                    .mimeType(mimeType)
                    .language(language)
                    .author(author)
                    .ocrText(ocrText != null ? ocrText.getExtractedText() : null)
                    .metadataText(null) // No plain metadataText field exists in ExtractedMetadata schema
                    .extractedKeywords(meta != null ? meta.getKeywords() : null)
                    .normalizedText(normalizedText)
                    .indexedAt(Instant.now())
                    .currentVersion(true)
                    .permissionHash("authenticated_user") // Mock permission hash
                    .vectorStatus("NOT_GENERATED")
                    .searchMetadata(new HashMap<>())
                    .build();

            // Transactional Replacement (Refinement 2)
            documentRepository.deleteById(doc.getDocumentId());
            documentRepository.save(doc);

            // Delegate to index providers
            SearchProvider provider = providerResolver.resolve(properties.provider());
            provider.index(doc);

            long duration = System.currentTimeMillis() - start;
            job.transitionToCompleted(duration);
            jobRepository.save(job);

            eventPublisher.publishEvent(new SearchIndexedEvent(
                    job.getJobId(), job.getDocumentId(), job.getVersionId(), tenantId, job.getIndexType().name(), properties.provider()
            ));

            log.info("Search indexing job {} completed successfully.", jobId);

        } catch (Exception e) {
            log.error("Exception in search indexing job: {}", jobId, e);
            markJobFailed(job, tenantId, e.getMessage());
        }
    }

    private void markJobFailed(SearchJob job, String tenantId, String error) {
        try {
            job.transitionToFailed(error != null && error.length() > 900 ? error.substring(0, 900) : error);
            jobRepository.save(job);
            eventPublisher.publishEvent(new SearchIndexFailedEvent(
                    job.getJobId(), job.getDocumentId(), job.getVersionId(), tenantId,
                    job.getIndexType().name(), properties.provider()
            ));
        } catch (Exception ex) {
            log.error("Failed to transition search job to FAILED state", ex);
        }
    }

    public ThreadPoolTaskExecutor getTaskExecutor() {
        return taskExecutor;
    }
}
