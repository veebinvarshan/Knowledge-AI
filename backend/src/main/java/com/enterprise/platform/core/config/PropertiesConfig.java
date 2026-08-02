package com.enterprise.platform.core.config;

import com.enterprise.platform.core.config.properties.*;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({
    JwtProperties.class,
    RedisProperties.class,
    MailProperties.class,
    StorageProperties.class,
    UploadProperties.class,
    AIProperties.class,
    RateLimitingProperties.class,
    ApiVersionProperties.class,
    CorsProperties.class,
    VirusScanProperties.class,
    MetadataProperties.class,
    ApacheTikaProperties.class,
    MetadataWorkerProperties.class,
    OcrProperties.class,
    OcrWorkerProperties.class,
    SearchProperties.class,
    LuceneProperties.class,
    SearchWorkerProperties.class,
    EmbeddingProperties.class,
    ChunkingProperties.class,
    EmbeddingWorkerProperties.class,
    SemanticSearchProperties.class,
    QueryEmbeddingProperties.class,
    SemanticSearchWorkerProperties.class,
    RagProperties.class,
    RagWorkerProperties.class
})
public class PropertiesConfig {}
