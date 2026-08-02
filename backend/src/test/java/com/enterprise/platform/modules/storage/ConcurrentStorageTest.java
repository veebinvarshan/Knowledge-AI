package com.enterprise.platform.modules.storage;

import com.enterprise.platform.core.config.properties.LocalStorageProperties;
import com.enterprise.platform.infrastructure.storage.LocalStorageProvider;
import com.enterprise.platform.modules.storage.service.dto.StorageLocation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;

public class ConcurrentStorageTest {

    @TempDir
    Path tempDir;

    private LocalStorageProvider provider;

    @BeforeEach
    void setUp() {
        LocalStorageProperties properties = new LocalStorageProperties(tempDir.toString());
        provider = new LocalStorageProvider(properties);
    }

    @Test
    void testConcurrentWritesDoNotCollide() throws Exception {
        int threadCount = 10;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        List<Future<StorageLocation>> futures = new ArrayList<>();

        for (int i = 0; i < threadCount; i++) {
            final int index = i;
            futures.add(executor.submit(() -> {
                ByteArrayInputStream in = new ByteArrayInputStream(("data-" + index).getBytes(StandardCharsets.UTF_8));
                return provider.store(in, "file-" + index + ".txt", "text/plain");
            }));
        }

        executor.shutdown();
        assertTrue(executor.awaitTermination(5, TimeUnit.SECONDS));

        List<String> objectKeys = new ArrayList<>();
        for (Future<StorageLocation> future : futures) {
            StorageLocation loc = future.get();
            assertNotNull(loc);
            assertFalse(objectKeys.contains(loc.providerObjectKey())); // No key collisions
            objectKeys.add(loc.providerObjectKey());
        }

        assertEquals(threadCount, objectKeys.size());
    }
}
