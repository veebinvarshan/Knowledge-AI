package com.enterprise.platform.core.config;

import com.enterprise.platform.infrastructure.storage.*;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

public class StorageConfigurationConditionalTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(
                    LocalStorageConfiguration.class,
                    AzureStorageConfiguration.class,
                    S3StorageConfiguration.class,
                    MinioStorageConfiguration.class
            ));

    @Test
    void testLocalStorageActive() {
        contextRunner
                .withPropertyValues(
                        "platform.storage.provider=local",
                        "platform.storage.local.root-directory=./temp-storage"
                )
                .run(context -> {
                    assertThat(context).hasSingleBean(LocalStorageProvider.class);
                    assertThat(context).doesNotHaveBean(AzureBlobStorageProvider.class);
                    assertThat(context).doesNotHaveBean(S3StorageProvider.class);
                    assertThat(context).doesNotHaveBean(MinIOStorageProvider.class);
                });
    }

    @Test
    void testAzureStorageActive() {
        contextRunner
                .withPropertyValues(
                        "platform.storage.provider=azure",
                        "platform.storage.azure.container=azure-container"
                )
                .run(context -> {
                    assertThat(context).hasSingleBean(AzureBlobStorageProvider.class);
                    assertThat(context).doesNotHaveBean(LocalStorageProvider.class);
                    assertThat(context).doesNotHaveBean(S3StorageProvider.class);
                    assertThat(context).doesNotHaveBean(MinIOStorageProvider.class);
                });
    }

    @Test
    void testS3StorageActive() {
        contextRunner
                .withPropertyValues(
                        "platform.storage.provider=s3",
                        "platform.storage.s3.bucket=s3-bucket",
                        "platform.storage.s3.region=us-east-1"
                )
                .run(context -> {
                    assertThat(context).hasSingleBean(S3StorageProvider.class);
                    assertThat(context).doesNotHaveBean(LocalStorageProvider.class);
                    assertThat(context).doesNotHaveBean(AzureBlobStorageProvider.class);
                    assertThat(context).doesNotHaveBean(MinIOStorageProvider.class);
                });
    }

    @Test
    void testMinioStorageActive() {
        contextRunner
                .withPropertyValues(
                        "platform.storage.provider=minio",
                        "platform.storage.minio.endpoint=http://localhost:9000",
                        "platform.storage.minio.bucket=minio-bucket"
                )
                .run(context -> {
                    assertThat(context).hasSingleBean(MinIOStorageProvider.class);
                    assertThat(context).doesNotHaveBean(LocalStorageProvider.class);
                    assertThat(context).doesNotHaveBean(AzureBlobStorageProvider.class);
                    assertThat(context).doesNotHaveBean(S3StorageProvider.class);
                });
    }
}
