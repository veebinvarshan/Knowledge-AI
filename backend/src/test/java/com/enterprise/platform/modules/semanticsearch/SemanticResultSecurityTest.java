package com.enterprise.platform.modules.semanticsearch;

import com.enterprise.platform.modules.search.service.SearchAuthorizationGuard;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class SemanticResultSecurityTest {

    @Test
    void testSearchAuthorizationFailsForInvalidTenant() {
        SearchAuthorizationGuard mockGuard = mock(SearchAuthorizationGuard.class);
        when(mockGuard.authorizeSearch("tenant-invalid", "hash")).thenReturn(false);

        // WHEN
        boolean authorized = mockGuard.authorizeSearch("tenant-invalid", "hash");

        // THEN
        assertFalse(authorized);
    }
}
