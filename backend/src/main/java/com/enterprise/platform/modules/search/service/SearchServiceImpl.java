package com.enterprise.platform.modules.search.service;

import com.enterprise.platform.core.config.properties.SearchProperties;
import com.enterprise.platform.modules.search.provider.SearchProvider;
import com.enterprise.platform.modules.search.provider.SearchResult;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class SearchServiceImpl implements SearchService {

    private final SearchProviderResolver providerResolver;
    private final SearchProperties properties;

    public SearchServiceImpl(SearchProviderResolver providerResolver, SearchProperties properties) {
        this.providerResolver = providerResolver;
        this.properties = properties;
    }

    @Override
    public SearchResult search(String query, String tenantId, String permissionHash, int limit) throws Exception {
        return search(query, tenantId, permissionHash, properties.provider(), limit);
    }

    @Override
    public SearchResult search(String query, String tenantId, String permissionHash, String searchType, int limit) throws Exception {
        String providerType = "HYBRID";
        if ("lexical".equalsIgnoreCase(searchType) || "LEXICAL".equalsIgnoreCase(searchType) || "LUCENE".equalsIgnoreCase(searchType)) {
            providerType = "LEXICAL";
        } else if ("semantic".equalsIgnoreCase(searchType) || "VECTOR".equalsIgnoreCase(searchType) || "QDRANT".equalsIgnoreCase(searchType)) {
            providerType = "VECTOR";
        }
        com.enterprise.platform.modules.search.provider.SearchProvider provider = providerResolver.resolve(providerType);
        return provider.search(query, tenantId, permissionHash, limit);
    }
}
