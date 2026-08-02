package com.enterprise.platform.modules.metadata;

import com.enterprise.platform.core.config.properties.ApacheTikaProperties;
import com.enterprise.platform.core.config.properties.MetadataProperties;
import com.enterprise.platform.core.config.properties.MetadataWorkerProperties;
import com.enterprise.platform.modules.metadata.domain.ExtractedMetadata;
import com.enterprise.platform.modules.metadata.domain.MetadataJob;
import com.enterprise.platform.modules.metadata.provider.MetadataExtractionResult;
import com.enterprise.platform.modules.metadata.provider.MetadataExtractor;
import com.enterprise.platform.modules.metadata.service.MetadataProviderResolver;
import com.enterprise.platform.modules.metadata.repository.ExtractedMetadataRepository;
import com.enterprise.platform.modules.metadata.repository.MetadataJobRepository;
import com.enterprise.platform.modules.metadata.service.MetadataExtractionServiceImpl;
import com.enterprise.platform.modules.storage.domain.StorageObject;
import com.enterprise.platform.modules.storage.repository.StorageObjectRepository;
import com.enterprise.platform.modules.storage.service.StorageService;
import com.enterprise.platform.modules.storage.service.dto.StorageResource;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.context.ApplicationEventPublisher;

import java.io.ByteArrayInputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class AdditionalMetadataValidationTest {

    @Test
    void testCanonicalFieldsArePrunedFromAdditionalMetadata() throws Exception {
        // GIVEN
        MetadataJobRepository jobRepository = mock(MetadataJobRepository.class);
        ExtractedMetadataRepository metadataRepository = mock(ExtractedMetadataRepository.class);
        StorageObjectRepository storageObjectRepository = mock(StorageObjectRepository.class);
        StorageService storageService = mock(StorageService.class);
        MetadataProviderResolver providerResolver = mock(MetadataProviderResolver.class);
        ApplicationEventPublisher eventPublisher = mock(ApplicationEventPublisher.class);
        MetadataProperties properties = new MetadataProperties(true, "TIKA", 3, 1000);
        ApacheTikaProperties tikaProperties = new ApacheTikaProperties(1000L, 5, 5, 2000);
        MetadataWorkerProperties workerProperties = new MetadataWorkerProperties(2, 5);

        MetadataExtractionServiceImpl service = new MetadataExtractionServiceImpl(
                jobRepository, metadataRepository, storageObjectRepository, storageService,
                providerResolver, eventPublisher, properties, tikaProperties, workerProperties
        );

        UUID jobId = UUID.randomUUID();
        UUID versionId = UUID.randomUUID();
        UUID docId = UUID.randomUUID();
        UUID storageObjectId = UUID.randomUUID();

        MetadataJob job = new MetadataJob(docId, versionId, storageObjectId, "tenant-1");
        job.setJobId(jobId);

        StorageObject storageObject = new StorageObject("test.pdf", "test-key", "LOCAL", "checksum", "SHA256", 100L, "application/pdf");
        StorageResource resource = mock(StorageResource.class);
        MetadataExtractor extractor = mock(MetadataExtractor.class);

        Map<String, Object> rawAdditional = new HashMap<>();
        rawAdditional.put("title", "Duplicate Title");
        rawAdditional.put("Author", "Duplicate Author");
        rawAdditional.put("Content-Type", "application/pdf");
        rawAdditional.put("custom-tag", "Unique custom tag value");

        MetadataExtractionResult result = new MetadataExtractionResult(
                "Real Title", "Subject", "Author", "Company", "Keywords", "en",
                10, 100, 500,
                null, null, null, null, null,
                "1.4", "Producer", "Creator", "false",
                null, null, null,
                null, null,
                rawAdditional
        );

        when(jobRepository.save(any(MetadataJob.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(jobRepository.findById(jobId)).thenReturn(Optional.of(job));
        when(storageObjectRepository.findById(storageObjectId)).thenReturn(Optional.of(storageObject));
        when(storageService.retrieve(anyString())).thenReturn(resource);
        when(resource.inputStream()).thenReturn(new ByteArrayInputStream(new byte[0]));
        when(providerResolver.resolve("application/pdf")).thenReturn(extractor);
        when(extractor.extract(any())).thenReturn(result);

        // WHEN
        service.executeExtraction(jobId, "tenant-1", UUID.randomUUID());

        // THEN
        ArgumentCaptor<ExtractedMetadata> captor = ArgumentCaptor.forClass(ExtractedMetadata.class);
        verify(metadataRepository, times(1)).save(captor.capture());
        
        ExtractedMetadata saved = captor.getValue();
        assertEquals("Real Title", saved.getTitle());
        
        // Assert pruned additionalMetadata fields
        assertFalse(saved.getAdditionalMetadata().containsKey("title"));
        assertFalse(saved.getAdditionalMetadata().containsKey("Author"));
        assertFalse(saved.getAdditionalMetadata().containsKey("Content-Type"));
        assertTrue(saved.getAdditionalMetadata().containsKey("custom-tag"));
        assertEquals("Unique custom tag value", saved.getAdditionalMetadata().get("custom-tag"));

        service.getTaskExecutor().shutdown();
    }
}
