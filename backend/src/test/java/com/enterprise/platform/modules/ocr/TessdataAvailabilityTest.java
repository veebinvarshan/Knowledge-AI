package com.enterprise.platform.modules.ocr;

import com.enterprise.platform.core.config.properties.OcrProperties;
import com.enterprise.platform.core.config.properties.TesseractProperties;
import com.enterprise.platform.modules.ocr.infrastructure.health.OcrHealthIndicator;
import com.enterprise.platform.modules.ocr.repository.OcrJobRepository;
import com.enterprise.platform.modules.ocr.service.OcrServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class TessdataAvailabilityTest {

    @Test
    void testTessdataReportedInHealthIndicator() {
        OcrProperties properties = new OcrProperties(true, "TESSERACT", "eng", 3, 1000, 70.0);
        TesseractProperties tesseractProperties = new TesseractProperties("./non_existent_folder_abc", 5000, 10, 150);
        OcrJobRepository jobRepository = mock(OcrJobRepository.class);
        OcrServiceImpl ocrService = mock(OcrServiceImpl.class);
        ThreadPoolTaskExecutor taskExecutor = mock(ThreadPoolTaskExecutor.class);

        when(ocrService.getTaskExecutor()).thenReturn(taskExecutor);
        when(taskExecutor.getActiveCount()).thenReturn(1);
        when(taskExecutor.getQueueSize()).thenReturn(5);

        org.springframework.beans.factory.ObjectProvider<TesseractProperties> tesseractPropertiesProvider = mock(org.springframework.beans.factory.ObjectProvider.class);
        when(tesseractPropertiesProvider.getIfAvailable(any())).thenReturn(tesseractProperties);

        OcrHealthIndicator indicator = new OcrHealthIndicator(properties, tesseractPropertiesProvider, jobRepository, ocrService);

        // WHEN
        Health health = indicator.health();

        // THEN (Folder does not exist, so indicator should return DOWN status)
        assertEquals(Status.DOWN, health.getStatus());
        assertEquals(false, health.getDetails().get("tessdataAccessibility"));
    }
}
