package com.enterprise.platform.modules.search.service;

import com.enterprise.platform.modules.search.provider.SearchResult;

public interface SearchService {
    SearchResult search(String query, String tenantId, String permissionHash, int limit) throws Exception;
    SearchResult search(String query, String tenantId, String permissionHash, String searchType, int limit) throws Exception;
}
