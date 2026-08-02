package com.enterprise.platform.modules.documents.upload.service.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UploadSessionInitDto(
    @NotBlank String fileName,
    @NotNull @Min(1) Long fileSizeBytes,
    @NotBlank String mimeType,
    @NotNull @Min(1) Integer chunksTotal
) {}
