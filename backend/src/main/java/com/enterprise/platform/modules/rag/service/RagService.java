package com.enterprise.platform.modules.rag.service;

import com.enterprise.platform.modules.rag.domain.RagRequest;
import com.enterprise.platform.modules.rag.domain.RagResponse;
import reactor.core.publisher.Flux;

public interface RagService {
    RagResponse generate(String tenantId, String permissionHash, RagRequest request);
    Flux<String> generateStream(String tenantId, String permissionHash, RagRequest request);
}
