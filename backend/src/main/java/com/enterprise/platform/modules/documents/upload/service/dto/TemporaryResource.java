package com.enterprise.platform.modules.documents.upload.service.dto;

import java.io.InputStream;

public record TemporaryResource(
    InputStream inputStream,
    long sizeBytes
) {}
