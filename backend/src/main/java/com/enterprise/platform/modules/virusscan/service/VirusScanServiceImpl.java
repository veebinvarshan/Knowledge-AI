package com.enterprise.platform.modules.virusscan.service;

import com.enterprise.platform.core.config.properties.VirusScanProperties;
import com.enterprise.platform.modules.documents.service.QuarantineGuard;
import com.enterprise.platform.modules.storage.domain.StorageObject;
import com.enterprise.platform.modules.storage.repository.StorageObjectRepository;
import com.enterprise.platform.modules.storage.service.StorageService;
import com.enterprise.platform.modules.storage.service.dto.StorageResource;
import com.enterprise.platform.modules.virusscan.domain.*;
import com.enterprise.platform.modules.virusscan.domain.VirusScanEvents.*;
import com.enterprise.platform.modules.virusscan.provider.*;
import com.enterprise.platform.modules.virusscan.repository.ScanJobRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.time.Instant;
import java.util.UUID;

@Service
@Transactional
public class VirusScanServiceImpl implements VirusScanService, QuarantineGuard {

    private static final Logger log = LoggerFactory.getLogger(VirusScanServiceImpl.class);

    private final ScanJobRepository repository;
    private final StorageObjectRepository storageObjectRepository;
    private final StorageService storageService;
    private final VirusScannerProviderResolver providerResolver;
    private final QuarantinePolicy quarantinePolicy;
    private final ApplicationEventPublisher eventPublisher;
    private final VirusScanProperties properties;
    private final ThreadPoolTaskExecutor taskExecutor;

    public VirusScanServiceImpl(
            ScanJobRepository repository,
            StorageObjectRepository storageObjectRepository,
            StorageService storageService,
            VirusScannerProviderResolver providerResolver,
            QuarantinePolicy quarantinePolicy,
            ApplicationEventPublisher eventPublisher,
            VirusScanProperties properties) {
        this.repository = repository;
        this.storageObjectRepository = storageObjectRepository;
        this.storageService = storageService;
        this.providerResolver = providerResolver;
        this.quarantinePolicy = quarantinePolicy;
        this.eventPublisher = eventPublisher;
        this.properties = properties;

        // Thread pool initialization with queue limits and backpressure
        this.taskExecutor = new ThreadPoolTaskExecutor();
        this.taskExecutor.setCorePoolSize(properties.workerThreads());
        this.taskExecutor.setMaxPoolSize(properties.workerThreads());
        this.taskExecutor.setQueueCapacity(properties.queueCapacity());
        this.taskExecutor.setThreadNamePrefix("virusscan-worker-");
        this.taskExecutor.setRejectedExecutionHandler(new java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy());
        this.taskExecutor.initialize();
    }

    @Override
    public ScanJob submitScanJob(String tenantId, UUID userId, UUID documentId, UUID versionId, UUID storageObjectId) {
        if (!properties.enabled()) {
            log.info("Virus scanning is disabled; bypassing scan submission.");
            return null;
        }

        ScanJob job = new ScanJob(documentId, versionId, storageObjectId);
        job = repository.save(job);

        eventPublisher.publishEvent(new VirusScanRequestedEvent(job.getId(), documentId, versionId, tenantId, userId));

        // Submit task asynchronously
        final UUID scanJobId = job.getId();
        taskExecutor.submit(() -> executeScan(scanJobId, tenantId, userId));

        return job;
    }

    @Override
    public void executeScan(UUID scanJobId, String tenantId, UUID userId) {
        ScanJob job = repository.findById(scanJobId).orElse(null);
        if (job == null) return;

        // Transition status to SCANNING
        try {
            job.transitionToScanning();
            job = repository.save(job);
        } catch (Exception e) {
            log.error("Failed to transition scan job to scanning state: {}", scanJobId, e);
            return;
        }

        eventPublisher.publishEvent(new VirusScanStartedEvent(job.getId(), job.getDocumentId(), job.getVersionId(), tenantId, userId));

        StorageObject storageObject = storageObjectRepository.findById(job.getStorageObjectId()).orElse(null);
        if (storageObject == null) {
            markScanFailed(job, tenantId, userId, "StorageObject metadata not found");
            return;
        }

        long start = System.currentTimeMillis();
        try {
            StorageResource resource = storageService.retrieve(storageObject.getLogicalPath());
            VirusScanner scanner = providerResolver.resolve();

            VirusScanResult result;
            try (InputStream in = resource.inputStream()) {
                result = scanner.scan(in);
            }

            long duration = System.currentTimeMillis() - start;

            if (result.status() == ScanJobStatus.CLEAN) {
                job.transitionToClean(result.engineName(), result.engineVersion(), duration, result.bytesScanned());
                repository.save(job);
                eventPublisher.publishEvent(new VirusScanCompletedEvent(job.getId(), job.getDocumentId(), job.getVersionId(), tenantId, userId));
                log.info("Scan job {} completed CLEAN. Scanned {} bytes in {} ms.", scanJobId, result.bytesScanned(), duration);
            } else if (result.status() == ScanJobStatus.INFECTED) {
                job.transitionToInfected(result.engineName(), result.engineVersion(), result.signatureName(), duration, result.bytesScanned());
                repository.save(job);

                // Delegate quarantine decision
                quarantinePolicy.execute(job, result);

                // Publish document quarantined event (the default policy does this but we also trigger versioned event details)
                eventPublisher.publishEvent(new DocumentQuarantinedEvent(job.getId(), job.getDocumentId(), job.getVersionId(), tenantId, userId));
                log.warn("Scan job {} completed INFECTED. Signature [{}]. Quarantined.", scanJobId, result.signatureName());
            } else {
                markScanFailed(job, tenantId, userId, "Scan returned failure result");
            }
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - start;
            log.error("Scan job {} exception after {} ms: {}", scanJobId, duration, e.getMessage());
            markScanFailed(job, tenantId, userId, e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isQuarantined(UUID versionId) {
        return repository.findByVersionId(versionId)
                .map(job -> job.getStatus() == ScanJobStatus.QUARANTINED || job.getStatus() == ScanJobStatus.INFECTED)
                .orElse(false);
    }

    private void markScanFailed(ScanJob job, String tenantId, UUID userId, String error) {
        try {
            job.transitionToFailed();
            repository.save(job);
            eventPublisher.publishEvent(new VirusScanFailedEvent(job.getId(), job.getDocumentId(), job.getVersionId(), tenantId, userId));
        } catch (Exception ex) {
            log.error("Failed to transition job status to FAILED", ex);
        }
    }

    public ThreadPoolTaskExecutor getTaskExecutor() {
        return taskExecutor;
    }
}
