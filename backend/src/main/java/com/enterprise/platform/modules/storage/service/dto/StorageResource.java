package com.enterprise.platform.modules.storage.service.dto;

import java.io.InputStream;

public record StorageResource(
    InputStream inputStream,
    StorageMetadata metadata
) {}
