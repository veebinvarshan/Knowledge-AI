package com.enterprise.platform.infrastructure.ratelimit;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

@Component
public class RateLimitKeyResolver {

    public String resolveByIp(HttpServletRequest request) {
        String xf = request.getHeader("X-Forwarded-For");
        if (xf != null && !xf.isEmpty()) {
            return xf.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    public String resolveByUser(String userId, HttpServletRequest request) {
        if (userId != null && !userId.isEmpty()) {
            return "user:" + userId;
        }
        return resolveByIp(request);
    }

    public String resolveByTenant(String tenantId, HttpServletRequest request) {
        if (tenantId != null && !tenantId.isEmpty()) {
            return "tenant:" + tenantId;
        }
        return resolveByIp(request);
    }
}
