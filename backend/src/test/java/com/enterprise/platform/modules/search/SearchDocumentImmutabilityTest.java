package com.enterprise.platform.modules.search;

import com.enterprise.platform.modules.search.domain.SearchDocument;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import static org.junit.jupiter.api.Assertions.*;

public class SearchDocumentImmutabilityTest {

    @Test
    void testSearchDocumentHasNoSetters() {
        Class<?> clazz = SearchDocument.class;
        Method[] methods = clazz.getDeclaredMethods();
        
        for (Method method : methods) {
            if (Modifier.isPublic(method.getModifiers())) {
                assertFalse(method.getName().startsWith("set"), 
                        "SearchDocument exposes a public setter: " + method.getName());
            }
        }
    }
}
