package com.enterprise.platform.modules.documents.api;

import jakarta.validation.constraints.NotNull;
import java.util.Map;

public class DocumentMetadataUpdateRequest {

    @NotNull(message = "Metadata payload cannot be null")
    private Map<String, Object> metadata;

    public DocumentMetadataUpdateRequest() {}

    public DocumentMetadataUpdateRequest(Map<String, Object> metadata) {
        this.metadata = metadata;
    }

    public Map<String, Object> getMetadata() { return metadata; }
    public void setMetadata(Map<String, Object> metadata) { this.metadata = metadata; }
}
