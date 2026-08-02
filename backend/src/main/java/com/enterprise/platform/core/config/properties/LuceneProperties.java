package com.enterprise.platform.core.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;
import jakarta.validation.constraints.NotBlank;

@ConfigurationProperties(prefix = "platform.search.lucene")
@Validated
public record LuceneProperties(
    @NotBlank String indexDir
) {}
