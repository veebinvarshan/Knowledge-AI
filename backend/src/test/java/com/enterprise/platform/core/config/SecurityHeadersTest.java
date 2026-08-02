package com.enterprise.platform.core.config;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.*;

public class SecurityHeadersTest {

    @Test
    void testSecurityHeadersInjected() throws Exception {
        // GIVEN
        NetworkSecurityConfig config = new NetworkSecurityConfig();
        Filter filter = config.securityHeadersFilter(true); // HTTPS enabled = true

        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain chain = mock(FilterChain.class);

        // WHEN
        filter.doFilter(request, response, chain);

        // THEN
        verify(response).setHeader("X-Content-Type-Options", "nosniff");
        verify(response).setHeader("X-Frame-Options", "DENY");
        verify(response).setHeader("Referrer-Policy", "no-referrer");
        verify(response).setHeader("Content-Security-Policy", "default-src 'self'");
        verify(response).setHeader("Cross-Origin-Opener-Policy", "same-origin");
        verify(response).setHeader("Cross-Origin-Resource-Policy", "same-origin");
        verify(response).setHeader("Strict-Transport-Security", "max-age=31536000; includeSubDomains");
        verify(chain).doFilter(request, response);
    }

    @Test
    void testHstsDisabledWhenHttpsDisabled() throws Exception {
        // GIVEN
        NetworkSecurityConfig config = new NetworkSecurityConfig();
        Filter filter = config.securityHeadersFilter(false); // HTTPS enabled = false

        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain chain = mock(FilterChain.class);

        // WHEN
        filter.doFilter(request, response, chain);

        // THEN (HSTS header must not be set)
        verify(response, never()).setHeader(eq("Strict-Transport-Security"), anyString());
    }
}
