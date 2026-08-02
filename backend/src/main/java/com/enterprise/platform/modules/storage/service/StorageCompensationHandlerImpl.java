package com.enterprise.platform.modules.storage.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class StorageCompensationHandlerImpl implements StorageCompensationHandler {

    private static final Logger log = LoggerFactory.getLogger(StorageCompensationHandlerImpl.class);

    private final ThreadLocal<List<CompensationAction>> actionsHolder = ThreadLocal.withInitial(ArrayList::new);
    private final Map<String, StorageProvider> providersMap;

    public StorageCompensationHandlerImpl(List<StorageProvider> providers) {
        this.providersMap = providers.stream()
                .collect(Collectors.toMap(
                        p -> p.getProviderId().toUpperCase(),
                        p -> p
                ));
    }

    @Override
    public void registerDeletion(String providerId, String objectKey) {
        actionsHolder.get().add(new CompensationAction(providerId, objectKey));
    }

    @Override
    public void executeCompensations() {
        List<CompensationAction> actions = actionsHolder.get();
        if (actions.isEmpty()) {
            return;
        }
        log.info("Executing storage compensation cleanup for {} actions...", actions.size());
        for (CompensationAction action : actions) {
            try {
                StorageProvider provider = providersMap.get(action.providerId().toUpperCase());
                if (provider != null) {
                    provider.delete(action.objectKey());
                    log.info("Compensation deleted orphaned object: [{}] on provider [{}]", action.objectKey(), action.providerId());
                }
            } catch (Exception e) {
                log.error("Failed to execute storage compensation for objectKey: {}", action.objectKey(), e);
            }
        }
        clearCompensations();
    }

    @Override
    public void clearCompensations() {
        actionsHolder.get().clear();
    }

    private record CompensationAction(String providerId, String objectKey) {}
}
