package com.enterprise.platform.core.correlation;

import com.enterprise.platform.modules.authorization.domain.AuthorizationContext;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.UUID;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class CorrelationFilter implements Filter {

    private static final String PREFERRED_HEADER = "X-Correlation-ID";
    private static final String FALLBACK_HEADER = "X-Request-ID";

    private static final String MDC_REQUEST_ID = "requestId";
    private static final String MDC_TRACE_ID = "traceId";
    private static final String MDC_TENANT_ID = "tenantId";
    private static final String MDC_USER_ID = "userId";

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        if (request instanceof HttpServletRequest httpReq && response instanceof HttpServletResponse httpRes) {
            String correlationId = httpReq.getHeader(PREFERRED_HEADER);
            if (correlationId == null || correlationId.trim().isEmpty()) {
                correlationId = httpReq.getHeader(FALLBACK_HEADER);
            }
            if (correlationId == null || correlationId.trim().isEmpty()) {
                correlationId = UUID.randomUUID().toString();
            }

            // Propagate response header
            httpRes.setHeader(PREFERRED_HEADER, correlationId);

            // Fetch tenant and user context from SecurityContextHolder
            String tenantId = null;
            UUID userId = null;
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.getPrincipal() instanceof AuthorizationContext authCtx) {
                tenantId = authCtx.getTenantId();
                userId = authCtx.getUserId();
            }

            // Populate MDC
            MDC.put(MDC_REQUEST_ID, correlationId);
            MDC.put(MDC_TRACE_ID, correlationId);
            if (tenantId != null) {
                MDC.put(MDC_TENANT_ID, tenantId);
            }
            if (userId != null) {
                MDC.put(MDC_USER_ID, userId.toString());
            }

            // Initialize RequestContext
            final String finalCorrelationId = correlationId;
            final String finalTenantId = tenantId;
            final UUID finalUserId = userId;
            final String clientIp = getClientIp(httpReq);
            final String userAgent = httpReq.getHeader("User-Agent");

            RequestContext ctx = new RequestContext() {
                @Override
                public String getRequestId() { return finalCorrelationId; }
                @Override
                public String getTenantId() { return finalTenantId; }
                @Override
                public UUID getAuthenticatedUserId() { return finalUserId; }
                @Override
                public String getClientIp() { return clientIp; }
                @Override
                public String getUserAgent() { return userAgent; }
            };

            RequestContextHolder.setContext(ctx);

            try {
                chain.doFilter(request, response);
            } finally {
                MDC.clear();
                RequestContextHolder.clearContext();
            }
        } else {
            chain.doFilter(request, response);
        }
    }

    private String getClientIp(HttpServletRequest request) {
        String xf = request.getHeader("X-Forwarded-For");
        if (xf != null && !xf.isEmpty()) {
            return xf.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
