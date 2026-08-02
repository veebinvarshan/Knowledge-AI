package com.enterprise.platform.modules.metadata;

import com.enterprise.platform.modules.metadata.provider.MetadataExtractor;
import com.enterprise.platform.modules.metadata.provider.PdfMetadataExtractor;
import com.enterprise.platform.modules.metadata.provider.OfficeMetadataExtractor;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ApacheTikaIsolationTest {

    @Test
    void testTikaClassesAreIsolatedBehindSpi() {
        // Assert that the public interfaces (MetadataExtractor) do not leak org.apache.tika class types
        Class<?> extractorClass = MetadataExtractor.class;
        for (java.lang.reflect.Method method : extractorClass.getDeclaredMethods()) {
            // Verify return type is not from apache.tika
            assertFalse(method.getReturnType().getName().contains("org.apache.tika"),
                    "SPI interface leaks Apache Tika classes in return values");
            // Verify parameter types do not leak apache.tika
            for (Class<?> paramType : method.getParameterTypes()) {
                assertFalse(paramType.getName().contains("org.apache.tika"),
                        "SPI interface leaks Apache Tika classes in parameters");
            }
        }
        
        // Assert concrete classes implement the SPI
        assertTrue(MetadataExtractor.class.isAssignableFrom(PdfMetadataExtractor.class));
        assertTrue(MetadataExtractor.class.isAssignableFrom(OfficeMetadataExtractor.class));
    }
}
