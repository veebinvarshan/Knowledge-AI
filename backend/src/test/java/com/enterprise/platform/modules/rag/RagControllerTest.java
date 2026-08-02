package com.enterprise.platform.modules.rag;

import com.enterprise.platform.modules.rag.api.RagController;
import com.enterprise.platform.modules.rag.domain.RagRequest;
import com.enterprise.platform.modules.rag.domain.RagResponse;
import com.enterprise.platform.modules.rag.service.RagService;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.util.Collections;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class RagControllerTest {

    @Test
    void testControllerGeneratesOk() {
        RagService service = mock(RagService.class);
        RagRequest req = new RagRequest("query", UUID.randomUUID(), Map.of(), "HYBRID", 2000);
        RagResponse resp = new RagResponse("ans", Collections.emptyList(), Map.of(), "SUCCESS", 200L);

        when(service.generate("tenant", "hash", req)).thenReturn(resp);

        RagController controller = new RagController(service);

        // WHEN
        ResponseEntity<RagResponse> responseEntity = controller.generate("tenant", "hash", req);

        // THEN
        assertNotNull(responseEntity);
        assertEquals(200, responseEntity.getStatusCode().value());
        assertEquals("ans", responseEntity.getBody().text());
    }
}
