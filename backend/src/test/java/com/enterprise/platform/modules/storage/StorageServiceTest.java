package com.enterprise.platform.modules.storage;

import com.enterprise.platform.modules.storage.domain.StorageObject;
import com.enterprise.platform.modules.storage.repository.StorageObjectRepository;
import com.enterprise.platform.modules.storage.service.*;
import com.enterprise.platform.modules.storage.service.dto.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class StorageServiceTest {

    private StorageObjectRepository repository;
    private StorageProviderResolver providerResolver;
    private StorageProvider activeProvider;
    private StorageCompensationHandler compensationHandler;
    private ApplicationEventPublisher eventPublisher;
    private StorageService storageService;

    @BeforeEach
    void setUp() {
        repository = mock(StorageObjectRepository.class);
        providerResolver = mock(StorageProviderResolver.class);
        activeProvider = mock(StorageProvider.class);
        compensationHandler = mock(StorageCompensationHandler.class);
        eventPublisher = mock(ApplicationEventPublisher.class);

        when(providerResolver.resolveActiveProvider()).thenReturn(activeProvider);

        storageService = new StorageServiceImpl(
                repository,
                providerResolver,
                compensationHandler,
                eventPublisher
        );
    }

    @Test
    void testStoreObjectPipelineSuccess() throws Exception {
        // GIVEN
        String content = "Hello world";
        InputStream in = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        String logicalPath = "hello.txt";

        StorageLocation mockLocation = new StorageLocation("LOCAL", logicalPath, "uuid-key.txt");
        StorageMetadata mockMeta = new StorageMetadata(11, "text/plain", "abc-checksum", "SHA256");

        when(repository.existsByLogicalPath(logicalPath)).thenReturn(false);
        when(activeProvider.store(eq(in), eq(logicalPath), eq("text/plain"))).thenReturn(mockLocation);
        when(activeProvider.readMetadata("uuid-key.txt")).thenReturn(mockMeta);
        
        when(repository.save(any(StorageObject.class))).thenAnswer(invocation -> {
            StorageObject obj = invocation.getArgument(0);
            obj.setId(java.util.UUID.randomUUID());
            return obj;
        });

        // WHEN
        StorageObject result = storageService.store(in, logicalPath, "text/plain");

        // THEN
        assertNotNull(result);
        assertEquals(logicalPath, result.getLogicalPath());
        assertEquals("uuid-key.txt", result.getProviderObjectKey());
        assertEquals("abc-checksum", result.getChecksum());
        verify(repository).save(any(StorageObject.class));
    }
}
