package com.enterprise.platform.modules.storage.service;

public interface StorageCompensationHandler {
    void registerDeletion(String providerId, String objectKey);
    void executeCompensations();
    void clearCompensations();
}
