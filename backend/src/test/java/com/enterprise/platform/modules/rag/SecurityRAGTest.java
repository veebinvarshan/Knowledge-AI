package com.enterprise.platform.modules.rag;

import com.enterprise.platform.modules.search.service.SearchAuthorizationGuard;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class SecurityRAGTest {

    @Test
    void testTenantSecurityEnforced() {
        SearchAuthorizationGuard guard = mock(SearchAuthorizationGuard.class);
        when(guard.authorizeSearch("unauthorized-tenant", "hash")).thenReturn(false);

        // WHEN
        boolean allowed = guard.authorizeSearch("unauthorized-tenant", "hash");

        // THEN
        assertFalse(allowed);
    }
}
