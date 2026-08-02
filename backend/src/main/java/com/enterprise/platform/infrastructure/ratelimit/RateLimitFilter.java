package com.enterprise.platform.infrastructure.ratelimit;

import com.enterprise.platform.core.correlation.RequestContext;
import com.enterprise.platform.core.correlation.RequestContextHolder;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Clock;
import java.time.Instant;
import java.util.*;

@Component
@Order(2) // Executes after CorrelationFilter
public class RateLimitFilter implements Filter {

    private final RateLimiter rateLimiter;
    private final RateLimitKeyResolver keyResolver;
    private final ObjectMapper objectMapper;
    private final Clock clock;

    public RateLimitFilter(
            RateLimiter rateLimiter,
            RateLimitKeyResolver keyResolver,
            ObjectMapper objectMapper,
            Clock clock) {
        this.rateLimiter = rateLimiter;
        this.keyResolver = keyResolver;
        this.objectMapper = objectMapper;
        this.clock = clock;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        if (request instanceof HttpServletRequest httpReq && response instanceof HttpServletResponse httpRes) {
            String path = httpReq.getRequestURI();
            
            Optional<RateLimitPolicy> policyOpt = matchPolicy(path);
            if (policyOpt.isPresent()) {
                RateLimitPolicy policy = policyOpt.get();
                
                RequestContext ctx = RequestContextHolder.getContext();
                String tenantId = ctx != null ? ctx.getTenantId() : null;
                String userId = ctx != null && ctx.getAuthenticatedUserId() != null ? ctx.getAuthenticatedUserId().toString() : null;

                String key = keyResolver.resolveByUser(userId, httpReq);
                if (policy == RateLimitPolicy.AUTHENTICATION) {
                    key = keyResolver.resolveByIp(httpReq); // Map auth endpoint by IP address
                }

                if (!rateLimiter.isAllowed(key, policy)) {
                    long retryAfter = rateLimiter.getRetryAfterSeconds(key, policy);
                    sendErrorResponse(httpReq, httpRes, retryAfter);
                    return;
                }
            }
        }
        chain.doFilter(request, response);
    }

    private Optional<RateLimitPolicy> matchPolicy(String path) {
        if (path.contains("/api/v1/auth/login") || path.contains("/api/v1/auth/register")) {
            return Optional.of(RateLimitPolicy.AUTHENTICATION);
        }
        if (path.contains("/api/v1/auth/forgot-password") || path.contains("/api/v1/auth/reset-password")) {
            return Optional.of(RateLimitPolicy.PASSWORD_RESET);
        }
        if (path.contains("/api/v1/auth/verify-email")) {
            return Optional.of(RateLimitPolicy.EMAIL_VERIFICATION);
        }
        if (path.contains("/api/v1/ai/")) {
            return Optional.of(RateLimitPolicy.FUTURE_AI);
        }
        return Optional.empty();
    }

    private void sendErrorResponse(HttpServletRequest request, HttpServletResponse response, long retryAfter) throws IOException {
        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setHeader("Retry-After", String.valueOf(retryAfter));

        RequestContext ctx = RequestContextHolder.getContext();
        String reqId = ctx != null ? ctx.getRequestId() : UUID.randomUUID().toString();

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("success", false);
        body.put("timestamp", Instant.now(clock).toString());
        body.put("status", HttpStatus.TOO_MANY_REQUESTS.value());
        body.put("error", "RATE_LIMIT_EXCEEDED");
        body.put("message", "Too many requests. Please try again after " + retryAfter + " seconds.");
        body.put("path", request.getRequestURI());
        body.put("requestId", reqId);
        body.put("validationErrors", Collections.emptyList());

        response.getWriter().write(objectMapper.writeValueAsString(body));
    }
}
