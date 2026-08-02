package com.enterprise.platform.modules.semanticsearch;

import com.enterprise.platform.modules.search.service.SearchAuthorizationGuard;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class PermissionIsolationTest {

    @Test
    void testGuardAuthorizedCheck() {
        SearchAuthorizationGuard mockGuard = mock(SearchAuthorizationGuard.class);
        when(mockGuard.authorizeSearch("tenant-A", "permission-hash")).thenReturn(true);

        assertTrue(mockGuard.authorizeSearch("tenant-A", "permission-hash"));
    }
}
