package com.enterprise.platform.infrastructure.logging;

import com.enterprise.platform.core.correlation.RequestContext;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Aspect
@Component
public class PerformanceLogAspect {

    private static final Logger log = LoggerFactory.getLogger(PerformanceLogAspect.class);

    private final long slowRequestThresholdMs;

    public PerformanceLogAspect(
            @Value("${platform.logging.slow-request-threshold-ms:1000}") long slowRequestThresholdMs) {
        this.slowRequestThresholdMs = slowRequestThresholdMs;
    }

    @Around("within(@org.springframework.web.bind.annotation.RestController *)")
    public Object logPerformance(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        
        HttpServletRequest request = null;
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            request = attributes.getRequest();
        }

        String method = request != null ? request.getMethod() : "UNKNOWN";
        String uri = request != null ? request.getRequestURI() : "UNKNOWN";

        Object result = null;
        try {
            result = joinPoint.proceed();
            return result;
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            
            int status = 200;
            if (attributes != null) {
                HttpServletResponse response = attributes.getResponse();
                if (response != null) {
                    status = response.getStatus();
                }
            }

            com.enterprise.platform.core.correlation.RequestContext context = 
                    com.enterprise.platform.core.correlation.RequestContextHolder.getContext();
            
            String reqId = context != null ? context.getRequestId() : "UNKNOWN";
            String user = context != null && context.getAuthenticatedUserId() != null ? context.getAuthenticatedUserId().toString() : "ANONYMOUS";
            String tenant = context != null && context.getTenantId() != null ? context.getTenantId() : "ANONYMOUS";

            String logMsg = String.format("Request: [%s] Method: [%s] URI: [%s] Status: [%d] Duration: [%d ms] User: [%s] Tenant: [%s]",
                    reqId, method, uri, status, duration, user, tenant);

            if (duration >= slowRequestThresholdMs) {
                log.warn("SLOW REQUEST DETECTED: {}", logMsg);
            } else {
                log.info(logMsg);
            }
        }
    }
}
