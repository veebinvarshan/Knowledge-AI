package com.enterprise.platform.modules.virusscan.service;

import com.enterprise.platform.core.config.properties.VirusScanProperties;
import com.enterprise.platform.modules.virusscan.domain.ScanJob;
import com.enterprise.platform.modules.virusscan.domain.ScanJobStatus;
import com.enterprise.platform.modules.virusscan.repository.ScanJobRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

@Component
public class RetryScheduler {

    private static final Logger log = LoggerFactory.getLogger(RetryScheduler.class);

    private final ScanJobRepository repository;
    private final VirusScanService scanService;
    private final VirusScanProperties properties;

    public RetryScheduler(ScanJobRepository repository, VirusScanService scanService, VirusScanProperties properties) {
        this.repository = repository;
        this.scanService = scanService;
        this.properties = properties;
    }

    @Scheduled(fixedDelay = 10000) // Run retry scheduler scan every 10 seconds
    public void processPendingRetries() {
        if (!properties.enabled()) return;

        List<ScanJob> retriableJobs = repository.findAllByStatusInAndNextRetryAtBefore(
                List.of(ScanJobStatus.FAILED),
                Instant.now()
        );

        for (ScanJob job : retriableJobs) {
            if (job.getRetryCount() < properties.retryCount()) {
                long backoffDelay = properties.retryBackoffMs() * (long) Math.pow(2, job.getRetryCount());
                Instant nextRetryTime = Instant.now().plusMillis(backoffDelay);

                try {
                    job.transitionToPendingForRetry(nextRetryTime);
                    repository.save(job);

                    log.info("Scheduling scan job {} retry #{} (backoff delay: {} ms)", 
                            job.getId(), job.getRetryCount(), backoffDelay);
                    
                    // Re-submit scan
                    scanService.executeScan(job.getId(), "tenant-placeholder", null);
                } catch (Exception e) {
                    log.error("Failed to schedule retry for scan job: {}", job.getId(), e);
                }
            } else {
                log.warn("Exhausted retries for scan job: {}", job.getId());
            }
        }
    }
}
