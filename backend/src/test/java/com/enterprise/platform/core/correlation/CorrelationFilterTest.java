package com.enterprise.platform.core.correlation;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class CorrelationFilterTest {

    private CorrelationFilter filter;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private FilterChain chain;

    @BeforeEach
    void setUp() {
        filter = new CorrelationFilter();
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        chain = mock(FilterChain.class);
    }

    @Test
    void testCorrelationIDReadFromHeader() throws Exception {
        // GIVEN
        when(request.getHeader("X-Correlation-ID")).thenReturn("custom-trace-id-123");
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");

        // WHEN
        filter.doFilter(request, response, chain);

        // THEN
        verify(response).setHeader("X-Correlation-ID", "custom-trace-id-123");
        verify(chain).doFilter(request, response);

        // MDC must be cleared post-filter execution
        assertNull(MDC.get("requestId"));
        assertNull(RequestContextHolder.getContext());
    }

    @Test
    void testRequestContextPopulatedDuringExecution() throws Exception {
        // GIVEN
        when(request.getHeader("X-Correlation-ID")).thenReturn("trace-id-999");
        when(request.getHeader("X-Forwarded-For")).thenReturn("192.168.1.1");
        when(request.getHeader("User-Agent")).thenReturn("Mozilla Firefox");

        // Execute chain with custom assertion to read values while thread executes the filter
        doAnswer(invocation -> {
            RequestContext context = RequestContextHolder.getContext();
            assertNotNull(context);
            assertEquals("trace-id-999", context.getRequestId());
            assertEquals("192.168.1.1", context.getClientIp());
            assertEquals("Mozilla Firefox", context.getUserAgent());
            
            assertEquals("trace-id-999", MDC.get("requestId"));
            assertEquals("trace-id-999", MDC.get("traceId"));
            return null;
        }).when(chain).doFilter(request, response);

        // WHEN
        filter.doFilter(request, response, chain);
    }

    @Test
    void testMdcCleanedUpAfterExceptions() throws Exception {
        // GIVEN
        when(request.getHeader("X-Correlation-ID")).thenReturn("trace-id-error");
        doThrow(new RuntimeException("Chain execution error")).when(chain).doFilter(request, response);

        // WHEN / THEN (Assert exception bubbles up but MDC is cleared)
        assertThrows(RuntimeException.class, () ->
            filter.doFilter(request, response, chain)
        );

        assertNull(MDC.get("requestId"));
        assertNull(RequestContextHolder.getContext());
    }
}
