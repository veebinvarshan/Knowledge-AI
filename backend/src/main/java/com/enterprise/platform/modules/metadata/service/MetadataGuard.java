package com.enterprise.platform.modules.metadata.service;

import java.util.UUID;

public interface MetadataGuard {
    boolean isMetadataExtracted(UUID versionId);
}
