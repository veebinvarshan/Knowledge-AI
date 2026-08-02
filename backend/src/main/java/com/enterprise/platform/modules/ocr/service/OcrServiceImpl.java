package com.enterprise.platform.modules.ocr.service;

import com.enterprise.platform.core.config.properties.OcrProperties;
import com.enterprise.platform.core.config.properties.OcrWorkerProperties;
import com.enterprise.platform.core.config.properties.TesseractProperties;
import com.enterprise.platform.modules.ocr.domain.*;
import com.enterprise.platform.modules.ocr.domain.OcrEvents.*;
import com.enterprise.platform.modules.ocr.preprocessing.ImagePreprocessor;
import com.enterprise.platform.modules.ocr.preprocessing.PdfPageRenderer;
import com.enterprise.platform.modules.ocr.provider.OcrProvider;
import com.enterprise.platform.modules.ocr.provider.OcrResult;
import com.enterprise.platform.modules.ocr.repository.OcrJobRepository;
import com.enterprise.platform.modules.ocr.repository.OcrTextRepository;
import com.enterprise.platform.modules.storage.domain.StorageObject;
import com.enterprise.platform.modules.storage.repository.StorageObjectRepository;
import com.enterprise.platform.modules.storage.service.StorageService;
import com.enterprise.platform.modules.storage.service.dto.StorageResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadPoolExecutor;

@Service
@Transactional
public class OcrServiceImpl implements OcrService, OcrGuard {

    private static final Logger log = LoggerFactory.getLogger(OcrServiceImpl.class);

    private final OcrJobRepository jobRepository;
    private final OcrTextRepository textRepository;
    private final StorageObjectRepository storageObjectRepository;
    private final StorageService storageService;
    private final OcrProviderResolver providerResolver;
    private final ImagePreprocessor preprocessor;
    private final PdfPageRenderer pdfRenderer;
    private final ApplicationEventPublisher eventPublisher;

    private final OcrProperties properties;
    private final TesseractProperties tesseractProperties;
    private final ThreadPoolTaskExecutor taskExecutor;

    public OcrServiceImpl(
            OcrJobRepository jobRepository,
            OcrTextRepository textRepository,
            StorageObjectRepository storageObjectRepository,
            StorageService storageService,
            OcrProviderResolver providerResolver,
            ImagePreprocessor preprocessor,
            PdfPageRenderer pdfRenderer,
            ApplicationEventPublisher eventPublisher,
            OcrProperties properties,
            ObjectProvider<TesseractProperties> tesseractPropertiesProvider,
            OcrWorkerProperties workerProperties) {
        this.jobRepository = jobRepository;
        this.textRepository = textRepository;
        this.storageObjectRepository = storageObjectRepository;
        this.storageService = storageService;
        this.providerResolver = providerResolver;
        this.preprocessor = preprocessor;
        this.pdfRenderer = pdfRenderer;
        this.eventPublisher = eventPublisher;
        this.properties = properties;
        this.tesseractProperties = tesseractPropertiesProvider.getIfAvailable(() -> new TesseractProperties("./tessdata", 30000, 100, 300));

        // Custom task executor with queue limits and CallerRunsPolicy backpressure
        this.taskExecutor = new ThreadPoolTaskExecutor();
        this.taskExecutor.setCorePoolSize(workerProperties.threads());
        this.taskExecutor.setMaxPoolSize(workerProperties.threads());
        this.taskExecutor.setQueueCapacity(workerProperties.queueCapacity());
        this.taskExecutor.setThreadNamePrefix("ocr-worker-");
        this.taskExecutor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        this.taskExecutor.initialize();
    }

    @Override
    public OcrJob submitOcrJob(String tenantId, UUID userId, UUID documentId, UUID versionId, UUID storageObjectId) {
        if (!properties.enabled()) {
            log.info("OCR is disabled; bypassing job submission.");
            return null;
        }

        OcrJob job = new OcrJob(documentId, versionId, storageObjectId, tenantId);
        job = jobRepository.save(job);

        eventPublisher.publishEvent(new OcrRequestedEvent(
                job.getJobId(), documentId, versionId, tenantId, properties.provider(), properties.languages()
        ));

        // Submit task asynchronously
        final UUID jobId = job.getJobId();
        taskExecutor.submit(() -> executeOcr(jobId, tenantId, userId));

        return job;
    }

    @Override
    public void executeOcr(UUID jobId, String tenantId, UUID userId) {
        OcrJob job = jobRepository.findById(jobId).orElse(null);
        if (job == null) return;

        try {
            job.transitionToProcessing();
            job = jobRepository.save(job);
        } catch (Exception e) {
            log.error("Failed to transition OCR job to processing: {}", jobId, e);
            return;
        }

        eventPublisher.publishEvent(new OcrStartedEvent(
                job.getJobId(), job.getDocumentId(), job.getVersionId(), tenantId, properties.provider(), properties.languages()
        ));

        StorageObject storageObject = storageObjectRepository.findById(job.getStorageObjectId()).orElse(null);
        if (storageObject == null) {
            markJobFailed(job, tenantId, "StorageObject metadata not found");
            return;
        }

        long start = System.currentTimeMillis();
        try {
            StorageResource resource = storageService.retrieve(storageObject.getLogicalPath());

            // 1. Searchable PDF Check (Heuristic executes before rendering pages)
            if ("application/pdf".equalsIgnoreCase(storageObject.getMimeType())) {
                try (InputStream in = resource.inputStream()) {
                    if (pdfRenderer.isSearchablePdf(in)) {
                        log.info("PDF version {} is already searchable; skipping OCR.", job.getVersionId());
                        job.transitionToSkipped("Searchable PDF");
                        jobRepository.save(job);
                        eventPublisher.publishEvent(new OcrSkippedEvent(
                                job.getJobId(), job.getDocumentId(), job.getVersionId(), tenantId,
                                properties.provider(), properties.languages(), "Searchable PDF"
                        ));
                        return;
                    }
                }
            }

            OcrProvider provider = providerResolver.resolve(storageObject.getMimeType());
            String text = "";
            double confidence = 0.0;
            int pageCount = 0;
            List<Double> perPageConfidence = new ArrayList<>();
            Map<String, Object> boundaries = new HashMap<>();

            if ("application/pdf".equalsIgnoreCase(storageObject.getMimeType())) {
                // Page-by-page OCR execution
                try (InputStream in = resource.inputStream()) {
                    pageCount = pdfRenderer.getPageCount(in);
                }
                
                // Keep memory footprint constant: render & ocr page-by-page, then dispose
                StringBuilder textBuilder = new StringBuilder();
                int maxPages = Math.min(pageCount, tesseractProperties.maxPages());
                
                for (int page = 0; page < maxPages; page++) {
                    BufferedImage pageImg;
                    try (InputStream in = resource.inputStream()) {
                        pageImg = pdfRenderer.renderPage(in, page, tesseractProperties.maxImageResolutionDpi());
                    }
                    
                    BufferedImage processedImg = preprocessor.preprocess(pageImg);
                    
                    // Convert BufferedImage back to stream for provider scan
                    ByteArrayOutputStream os = new ByteArrayOutputStream();
                    ImageIO.write(processedImg, "png", os);
                    
                    OcrResult pageResult;
                    try (InputStream in = new ByteArrayInputStream(os.toByteArray())) {
                        pageResult = provider.ocr(in, properties.languages());
                    }
                    
                    int startIdx = textBuilder.length();
                    textBuilder.append(pageResult.extractedText()).append("\n");
                    int endIdx = textBuilder.length();
                    
                    boundaries.put("page_" + (page + 1), Map.of("start", startIdx, "end", endIdx));
                    perPageConfidence.add(pageResult.confidenceScore());
                    confidence += pageResult.confidenceScore();
                }
                text = textBuilder.toString().trim();
                confidence = pageCount > 0 ? confidence / pageCount : 0.0;
            } else {
                // Image OCR execution
                BufferedImage img;
                try (InputStream in = resource.inputStream()) {
                    img = ImageIO.read(in);
                }
                if (img != null) {
                    BufferedImage processedImg = preprocessor.preprocess(img);
                    ByteArrayOutputStream os = new ByteArrayOutputStream();
                    ImageIO.write(processedImg, "png", os);
                    
                    OcrResult result;
                    try (InputStream in = new ByteArrayInputStream(os.toByteArray())) {
                        result = provider.ocr(in, properties.languages());
                    }
                    text = result.extractedText();
                    confidence = result.confidenceScore();
                    pageCount = 1;
                    perPageConfidence.add(confidence);
                    boundaries.put("page_1", Map.of("start", 0, "end", text.length()));
                }
            }

            long duration = System.currentTimeMillis() - start;

            // Transactional Replacement (Refinement 2)
            textRepository.deleteById(job.getVersionId());

            // Build OcrText
            OcrText ocrText = OcrText.builder()
                    .versionId(job.getVersionId())
                    .documentId(job.getDocumentId())
                    .tenantId(tenantId)
                    .language(properties.languages())
                    .confidenceScore(confidence)
                    .pageCount(pageCount)
                    .extractedText(text)
                    .extractedAt(Instant.now())
                    .provider(provider.getClass().getSimpleName())
                    .pageBoundaries(boundaries)
                    .additionalMetadata(Map.of("perPageConfidence", perPageConfidence))
                    .build();

            textRepository.save(ocrText);

            job.transitionToCompleted(duration, confidence, pageCount, provider.getClass().getSimpleName());
            jobRepository.save(job);

            eventPublisher.publishEvent(new OcrCompletedEvent(
                    job.getJobId(), job.getDocumentId(), job.getVersionId(), tenantId,
                    provider.getClass().getSimpleName(), properties.languages()
            ));

            log.info("OCR job {} completed successfully. Pages scanned: {}.", jobId, pageCount);

        } catch (Exception e) {
            log.error("Exception in OCR job: {}", jobId, e);
            markJobFailed(job, tenantId, e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isOcrCompleted(UUID versionId) {
        return textRepository.findById(versionId).isPresent();
    }

    private void markJobFailed(OcrJob job, String tenantId, String error) {
        try {
            job.transitionToFailed(error != null && error.length() > 900 ? error.substring(0, 900) : error);
            jobRepository.save(job);
            eventPublisher.publishEvent(new OcrFailedEvent(
                    job.getJobId(), job.getDocumentId(), job.getVersionId(), tenantId,
                    properties.provider(), properties.languages()
            ));
        } catch (Exception ex) {
            log.error("Failed to transition OCR job to FAILED state", ex);
        }
    }

    public ThreadPoolTaskExecutor getTaskExecutor() {
        return taskExecutor;
    }
}
