package com.enterprise.platform.modules.storage.service;

import com.enterprise.platform.modules.storage.domain.StorageObject;
import com.enterprise.platform.modules.storage.domain.StorageEvents.*;
import com.enterprise.platform.modules.storage.exception.*;
import com.enterprise.platform.modules.storage.repository.StorageObjectRepository;
import com.enterprise.platform.modules.storage.service.dto.*;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.io.InputStream;
import java.util.UUID;

@Service
@Transactional
public class StorageServiceImpl implements StorageService {

    private final StorageObjectRepository repository;
    private final StorageProviderResolver providerResolver;
    private final StorageCompensationHandler compensationHandler;
    private final ApplicationEventPublisher eventPublisher;

    public StorageServiceImpl(
            StorageObjectRepository repository,
            StorageProviderResolver providerResolver,
            StorageCompensationHandler compensationHandler,
            ApplicationEventPublisher eventPublisher) {
        this.repository = repository;
        this.providerResolver = providerResolver;
        this.compensationHandler = compensationHandler;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public StorageObject store(InputStream inputStream, String logicalPath, String mimeType) throws StorageException {
        if (repository.existsByLogicalPath(logicalPath)) {
            throw new StorageWriteException("Storage object already exists at logical path: " + logicalPath);
        }

        StorageProvider provider = providerResolver.resolveActiveProvider();
        StorageLocation location = provider.store(inputStream, logicalPath, mimeType);

        // Register compensation in case transaction fails
        compensationHandler.registerDeletion(location.providerId(), location.providerObjectKey());

        StorageMetadata metadata = provider.readMetadata(location.providerObjectKey());

        StorageObject obj = new StorageObject(
                location.logicalPath(),
                location.providerObjectKey(),
                location.providerId(),
                metadata.checksum(),
                metadata.checksumAlgorithm(),
                metadata.sizeBytes(),
                metadata.mimeType()
        );

        obj = repository.save(obj);

        publishAfterCommit(new StorageObjectCreatedEvent(obj.getId(), location.providerId(), logicalPath));
        return obj;
    }

    @Override
    @Transactional(readOnly = true)
    public StorageResource retrieve(String logicalPath) throws StorageException {
        StorageObject obj = repository.findByLogicalPath(logicalPath)
                .orElseThrow(() -> new StorageNotFoundException("Storage object not found at logical path: " + logicalPath));

        StorageProvider provider = providerResolver.resolveActiveProvider();
        StorageResource resource = provider.retrieve(obj.getProviderObjectKey());

        // Verify checksum integrity
        if (!obj.getChecksum().equalsIgnoreCase(resource.metadata().checksum())) {
            throw new StorageReadException("Storage object integrity validation failed: checksum mismatch");
        }

        return resource;
    }

    @Override
    public void delete(String logicalPath) throws StorageException {
        StorageObject obj = repository.findByLogicalPath(logicalPath)
                .orElseThrow(() -> new StorageNotFoundException("Storage object not found at logical path: " + logicalPath));

        StorageProvider provider = providerResolver.resolveActiveProvider();
        provider.delete(obj.getProviderObjectKey());
        repository.delete(obj);

        publishAfterCommit(new StorageObjectDeletedEvent(obj.getId(), obj.getProviderId(), logicalPath));
    }

    @Override
    @Transactional(readOnly = true)
    public boolean exists(String logicalPath) {
        return repository.existsByLogicalPath(logicalPath);
    }

    @Override
    public StorageObject copy(String sourceLogicalPath, String destLogicalPath) throws StorageException {
        StorageObject source = repository.findByLogicalPath(sourceLogicalPath)
                .orElseThrow(() -> new StorageNotFoundException("Source storage object not found: " + sourceLogicalPath));

        if (repository.existsByLogicalPath(destLogicalPath)) {
            throw new StorageWriteException("Destination storage object already exists: " + destLogicalPath);
        }

        StorageProvider provider = providerResolver.resolveActiveProvider();
        provider.copy(source.getProviderObjectKey(), destLogicalPath);

        // Create metadata duplicate
        StorageObject dest = new StorageObject(
                destLogicalPath,
                UUID.randomUUID().toString(), // Managed destination key by copy
                source.getProviderId(),
                source.getChecksum(),
                source.getChecksumAlgorithm(),
                source.getSizeBytes(),
                source.getMimeType()
        );

        dest = repository.save(dest);

        publishAfterCommit(new StorageObjectCopiedEvent(dest.getId(), dest.getProviderId(), sourceLogicalPath, destLogicalPath));
        return dest;
    }

    @Override
    public StorageObject move(String sourceLogicalPath, String destLogicalPath) throws StorageException {
        StorageObject source = repository.findByLogicalPath(sourceLogicalPath)
                .orElseThrow(() -> new StorageNotFoundException("Source storage object not found: " + sourceLogicalPath));

        if (repository.existsByLogicalPath(destLogicalPath)) {
            throw new StorageWriteException("Destination storage object already exists: " + destLogicalPath);
        }

        StorageProvider provider = providerResolver.resolveActiveProvider();
        provider.move(source.getProviderObjectKey(), destLogicalPath);

        // Update path metadata
        source.setLogicalPath(destLogicalPath);
        source = repository.save(source);

        publishAfterCommit(new StorageObjectMovedEvent(source.getId(), source.getProviderId(), sourceLogicalPath, destLogicalPath));
        return source;
    }

    @Override
    @Transactional(readOnly = true)
    public SignedUrl generateSignedUrl(String logicalPath, long expirationSeconds, String httpMethod) throws StorageException {
        StorageObject obj = repository.findByLogicalPath(logicalPath)
                .orElseThrow(() -> new StorageNotFoundException("Storage object not found: " + logicalPath));

        StorageProvider provider = providerResolver.resolveActiveProvider();
        return provider.generateSignedUrl(obj.getProviderObjectKey(), expirationSeconds, httpMethod);
    }

    private void publishAfterCommit(Object event) {
        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    eventPublisher.publishEvent(event);
                }
                @Override
                public void afterCompletion(int status) {
                    if (status == TransactionSynchronization.STATUS_ROLLED_BACK) {
                        compensationHandler.executeCompensations();
                    } else {
                        compensationHandler.clearCompensations();
                    }
                }
            });
        } else {
            eventPublisher.publishEvent(event);
        }
    }
}
