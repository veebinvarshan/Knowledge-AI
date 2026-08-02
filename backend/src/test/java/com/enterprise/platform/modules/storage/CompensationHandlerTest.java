package com.enterprise.platform.modules.storage;

import com.enterprise.platform.modules.storage.service.StorageCompensationHandlerImpl;
import com.enterprise.platform.modules.storage.service.StorageProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.mockito.Mockito.*;

public class CompensationHandlerTest {

    private StorageProvider localProvider;
    private StorageCompensationHandlerImpl compensationHandler;

    @BeforeEach
    void setUp() {
        localProvider = mock(StorageProvider.class);
        when(localProvider.getProviderId()).thenReturn("LOCAL");

        compensationHandler = new StorageCompensationHandlerImpl(List.of(localProvider));
    }

    @Test
    void testCompensationDeletesPhysicalFileOnTrigger() throws Exception {
        // GIVEN
        compensationHandler.registerDeletion("LOCAL", "uuid-key-to-delete.pdf");

        // WHEN
        compensationHandler.executeCompensations();

        // THEN
        verify(localProvider, times(1)).delete("uuid-key-to-delete.pdf");
    }

    @Test
    void testClearCompensationsDoesNotExecute() throws Exception {
        // GIVEN
        compensationHandler.registerDeletion("LOCAL", "uuid-key-to-keep.pdf");
        compensationHandler.clearCompensations();

        // WHEN
        compensationHandler.executeCompensations();

        // THEN (Never invoked delete since the registrations list was cleared)
        verify(localProvider, never()).delete(anyString());
    }
}
