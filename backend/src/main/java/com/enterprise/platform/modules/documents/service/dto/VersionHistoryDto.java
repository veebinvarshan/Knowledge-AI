package com.enterprise.platform.modules.documents.service.dto;

import java.util.List;

public record VersionHistoryDto(
    List<LightweightVersionDto> items,
    int page,
    int size,
    long totalElements,
    int totalPages
) {}
