package com.enterprise.platform.modules.search.service;

public interface SearchAuthorizationGuard {
    boolean authorizeSearch(String tenantId, String permissionHash);
}
