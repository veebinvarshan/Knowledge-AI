package com.enterprise.platform.modules.ocr;

import com.enterprise.platform.modules.ocr.provider.OcrProvider;
import com.enterprise.platform.modules.ocr.service.OcrProviderResolver;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ProviderPriorityResolverTest {

    @Test
    void testResolverChoosesLowestOrderPriority() {
        OcrProvider mimeProvider = mock(OcrProvider.class);
        OcrProvider tesseractProvider = mock(OcrProvider.class);
        OcrProvider fallbackNoOpProvider = mock(OcrProvider.class);

        // Priority 1
        when(mimeProvider.supports("image/png")).thenReturn(true);
        when(mimeProvider.getPriority()).thenReturn(1);

        // Priority 2
        when(tesseractProvider.supports("image/png")).thenReturn(true);
        when(tesseractProvider.getPriority()).thenReturn(2);

        // Priority 100
        when(fallbackNoOpProvider.supports("image/png")).thenReturn(true);
        when(fallbackNoOpProvider.getPriority()).thenReturn(100);

        OcrProviderResolver resolver = new OcrProviderResolver(
                List.of(fallbackNoOpProvider, mimeProvider, tesseractProvider)
        );

        // WHEN
        OcrProvider resolved = resolver.resolve("image/png");

        // THEN
        assertEquals(mimeProvider, resolved);
    }
}
