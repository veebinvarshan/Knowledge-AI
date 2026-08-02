package com.enterprise.platform.modules.metadata.service;

import com.enterprise.platform.core.config.properties.ApacheTikaProperties;
import com.enterprise.platform.core.config.properties.MetadataProperties;
import com.enterprise.platform.core.config.properties.MetadataWorkerProperties;
import com.enterprise.platform.modules.metadata.domain.*;
import com.enterprise.platform.modules.metadata.domain.MetadataEvents.*;
import com.enterprise.platform.modules.metadata.provider.MetadataExtractionResult;
import com.enterprise.platform.modules.metadata.provider.MetadataExtractor;
import com.enterprise.platform.modules.metadata.repository.ExtractedMetadataRepository;
import com.enterprise.platform.modules.metadata.repository.MetadataJobRepository;
import com.enterprise.platform.modules.storage.domain.StorageObject;
import com.enterprise.platform.modules.storage.repository.StorageObjectRepository;
import com.enterprise.platform.modules.storage.service.StorageService;
import com.enterprise.platform.modules.storage.service.dto.StorageResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadPoolExecutor;

@Service
@Transactional
public class MetadataExtractionServiceImpl implements MetadataExtractionService, MetadataGuard {

    private static final Logger log = LoggerFactory.getLogger(MetadataExtractionServiceImpl.class);

    private final MetadataJobRepository jobRepository;
    private final ExtractedMetadataRepository metadataRepository;
    private final StorageObjectRepository storageObjectRepository;
    private final StorageService storageService;
    private final MetadataProviderResolver providerResolver;
    private final ApplicationEventPublisher eventPublisher;
    
    private final MetadataProperties properties;
    private final ApacheTikaProperties tikaProperties;
    private final ThreadPoolTaskExecutor taskExecutor;

    public MetadataExtractionServiceImpl(
            MetadataJobRepository jobRepository,
            ExtractedMetadataRepository metadataRepository,
            StorageObjectRepository storageObjectRepository,
            StorageService storageService,
            MetadataProviderResolver providerResolver,
            ApplicationEventPublisher eventPublisher,
            MetadataProperties properties,
            ApacheTikaProperties tikaProperties,
            MetadataWorkerProperties workerProperties) {
        this.jobRepository = jobRepository;
        this.metadataRepository = metadataRepository;
        this.storageObjectRepository = storageObjectRepository;
        this.storageService = storageService;
        this.providerResolver = providerResolver;
        this.eventPublisher = eventPublisher;
        this.properties = properties;
        this.tikaProperties = tikaProperties;

        // Custom task executor with queue configuration & CallerRunsPolicy backpressure
        this.taskExecutor = new ThreadPoolTaskExecutor();
        this.taskExecutor.setCorePoolSize(workerProperties.threads());
        this.taskExecutor.setMaxPoolSize(workerProperties.threads());
        this.taskExecutor.setQueueCapacity(workerProperties.queueCapacity());
        this.taskExecutor.setThreadNamePrefix("metadata-worker-");
        this.taskExecutor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        this.taskExecutor.initialize();
    }

    @Override
    public MetadataJob submitExtractionJob(String tenantId, UUID userId, UUID documentId, UUID versionId, UUID storageObjectId) {
        if (!properties.enabled()) {
            log.info("Metadata extraction is disabled; skipping submission.");
            return null;
        }

        MetadataJob job = new MetadataJob(documentId, versionId, storageObjectId, tenantId);
        job = jobRepository.save(job);

        eventPublisher.publishEvent(new MetadataExtractionRequestedEvent(
                job.getJobId(), documentId, versionId, tenantId, "TIKA"
        ));

        // Submit task asynchronously
        final UUID jobId = job.getJobId();
        taskExecutor.submit(() -> executeExtraction(jobId, tenantId, userId));

        return job;
    }

    @Override
    public void executeExtraction(UUID jobId, String tenantId, UUID userId) {
        MetadataJob job = jobRepository.findById(jobId).orElse(null);
        if (job == null) return;

        try {
            job.transitionToExtracting();
            job = jobRepository.save(job);
        } catch (Exception e) {
            log.error("Failed to transition metadata job to extracting state: {}", jobId, e);
            return;
        }

        eventPublisher.publishEvent(new MetadataExtractionStartedEvent(
                job.getJobId(), job.getDocumentId(), job.getVersionId(), tenantId, "TIKA"
        ));

        StorageObject storageObject = storageObjectRepository.findById(job.getStorageObjectId()).orElse(null);
        if (storageObject == null) {
            markJobFailed(job, tenantId, "StorageObject metadata not found");
            return;
        }

        long start = System.currentTimeMillis();
        try {
            StorageResource resource = storageService.retrieve(storageObject.getLogicalPath());
            MetadataExtractor extractor = providerResolver.resolve(storageObject.getMimeType());

            MetadataExtractionResult result;
            try (InputStream in = resource.inputStream()) {
                result = extractor.extract(in);
            }

            long duration = System.currentTimeMillis() - start;

            // Remove duplicated canonical fields from additionalMetadata mapping to save space and avoid inconsistency
            Map<String, Object> cleanAdditional = new HashMap<>(result.additionalMetadata());
            cleanAdditional.remove("title");
            cleanAdditional.remove("Author");
            cleanAdditional.remove("Creator");
            cleanAdditional.remove("Content-Type");
            cleanAdditional.remove("Content-Length");

            // Transactional Replacement (Refinement 2)
            metadataRepository.deleteById(job.getVersionId());

            // Build ExtractedMetadata
            ExtractedMetadata meta = ExtractedMetadata.builder()
                    .versionId(job.getVersionId())
                    .documentId(job.getDocumentId())
                    .tenantId(tenantId)
                    .mimeType(storageObject.getMimeType())
                    .extension(getFileExtension(storageObject.getLogicalPath()))
                    .size(storageObject.getSizeBytes())
                    .checksum(storageObject.getChecksum())
                    .checksumAlgorithm(storageObject.getChecksumAlgorithm())
                    .title(result.title())
                    .subject(result.subject())
                    .author(result.author())
                    .company(result.company())
                    .keywords(result.keywords())
                    .language(result.language())
                    .pageCount(result.pageCount())
                    .wordCount(result.wordCount())
                    .characterCount(result.characterCount())
                    .width(result.width())
                    .height(result.height())
                    .dpi(result.dpi())
                    .colorSpace(result.colorSpace())
                    .cameraModel(result.cameraModel())
                    .pdfVersion(result.pdfVersion())
                    .producer(result.producer())
                    .creator(result.creator())
                    .encryptionStatus(result.encryptionStatus())
                    .application(result.application())
                    .revision(result.revision())
                    .lastModifiedBy(result.lastModifiedBy())
                    .encoding(result.encoding())
                    .lineCount(result.lineCount())
                    .additionalMetadata(cleanAdditional)
                    .build();

            metadataRepository.save(meta);

            job.transitionToCompleted(duration, "TIKA");
            jobRepository.save(job);

            eventPublisher.publishEvent(new MetadataExtractionCompletedEvent(
                    job.getJobId(), job.getDocumentId(), job.getVersionId(), tenantId, "TIKA"
            ));

            log.info("Metadata extraction job {} completed successfully in {} ms.", jobId, duration);

        } catch (Exception e) {
            log.error("Exception in metadata extraction job: {}", jobId, e);
            markJobFailed(job, tenantId, e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isMetadataExtracted(UUID versionId) {
        return metadataRepository.findById(versionId).isPresent();
    }

    private void markJobFailed(MetadataJob job, String tenantId, String error) {
        try {
            job.transitionToFailed(error != null && error.length() > 900 ? error.substring(0, 900) : error);
            jobRepository.save(job);
            eventPublisher.publishEvent(new MetadataExtractionFailedEvent(
                    job.getJobId(), job.getDocumentId(), job.getVersionId(), tenantId, "TIKA"
            ));
        } catch (Exception ex) {
            log.error("Failed to transition metadata job to FAILED state", ex);
        }
    }

    private String getFileExtension(String path) {
        if (path == null) return null;
        int idx = path.lastIndexOf('.');
        return idx == -1 ? "" : path.substring(idx + 1);
    }

    public ThreadPoolTaskExecutor getTaskExecutor() {
        return taskExecutor;
    }
}
