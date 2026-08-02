package com.enterprise.platform.modules.search.service;

import org.springframework.stereotype.Service;

@Service
public class SearchAuthorizationGuardImpl implements SearchAuthorizationGuard {
    @Override
    public boolean authorizeSearch(String tenantId, String permissionHash) {
        // Allow all non-null tenant/permissions in baseline setup
        return tenantId != null && permissionHash != null;
    }
}
