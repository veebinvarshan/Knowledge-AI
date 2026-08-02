package com.enterprise.platform.modules.metadata;

import com.enterprise.platform.modules.metadata.domain.ExtractedMetadata;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import static org.junit.jupiter.api.Assertions.*;

public class MetadataImmutabilityTest {

    @Test
    void testExtractedMetadataHasNoSetterMethods() {
        Class<?> clazz = ExtractedMetadata.class;
        for (Method method : clazz.getDeclaredMethods()) {
            if (method.getName().startsWith("set") && Modifier.isPublic(method.getModifiers())) {
                fail("Mutator/Setter method detected on ExtractedMetadata: " + method.getName());
            }
        }
    }
}
