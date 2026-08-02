package com.enterprise.platform.modules.ocr.infrastructure.health;

import com.enterprise.platform.core.config.properties.OcrProperties;
import com.enterprise.platform.core.config.properties.TesseractProperties;
import com.enterprise.platform.modules.ocr.domain.OcrJobStatus;
import com.enterprise.platform.modules.ocr.repository.OcrJobRepository;
import com.enterprise.platform.modules.ocr.service.OcrServiceImpl;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import java.io.File;
import java.time.Instant;
import java.util.List;

@Component("ocrHealthIndicator")
public class OcrHealthIndicator implements HealthIndicator {

    private final OcrProperties properties;
    private final TesseractProperties tesseractProperties;
    private final OcrJobRepository jobRepository;
    private final OcrServiceImpl ocrService;

    public OcrHealthIndicator(
            OcrProperties properties,
            ObjectProvider<TesseractProperties> tesseractPropertiesProvider,
            OcrJobRepository jobRepository,
            OcrServiceImpl ocrService) {
        this.properties = properties;
        this.tesseractProperties = tesseractPropertiesProvider.getIfAvailable(() -> new TesseractProperties("./tessdata", 30000, 100, 300));
        this.jobRepository = jobRepository;
        this.ocrService = ocrService;
    }

    @Override
    public Health health() {
        if (!properties.enabled()) {
            return Health.up().withDetail("status", "Disabled by configuration").build();
        }

        boolean tessdataExists = false;
        try {
            File folder = new File(tesseractProperties.tessdataPath());
            tessdataExists = folder.exists() && folder.isDirectory();
        } catch (Exception e) {
            // Ignore
        }

        ThreadPoolTaskExecutor executor = ocrService.getTaskExecutor();
        int activeWorkers = executor.getActiveCount();
        int queueSize = executor.getQueueSize();

        long retryBacklog = 0;
        try {
            retryBacklog = jobRepository.findAllByStatusInAndStartedAtBefore(
                    List.of(OcrJobStatus.FAILED, OcrJobStatus.RETRYING),
                    Instant.now()
            ).size();
        } catch (Exception e) {
            // Safe fallback
        }

        Health.Builder builder = tessdataExists ? Health.up() : Health.down();
        return builder
                .withDetail("provider", properties.provider())
                .withDetail("tessdataAccessibility", tessdataExists)
                .withDetail("activeWorkers", activeWorkers)
                .withDetail("queueSize", queueSize)
                .withDetail("retryBacklog", retryBacklog)
                .build();
    }
}
