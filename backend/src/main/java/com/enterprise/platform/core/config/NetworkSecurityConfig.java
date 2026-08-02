package com.enterprise.platform.core.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.ShallowEtagHeaderFilter;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@Configuration
public class NetworkSecurityConfig {

    @Bean
    public ShallowEtagHeaderFilter shallowEtagHeaderFilter() {
        return new ShallowEtagHeaderFilter();
    }

    @Bean
    public Filter securityHeadersFilter(
            @Value("${platform.security.https-enabled:false}") boolean httpsEnabled) {
        return new Filter() {
            @Override
            public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
                    throws IOException, ServletException {
                if (response instanceof HttpServletResponse httpRes) {
                    httpRes.setHeader("X-Content-Type-Options", "nosniff");
                    httpRes.setHeader("X-Frame-Options", "DENY");
                    httpRes.setHeader("Referrer-Policy", "no-referrer");
                    httpRes.setHeader("Content-Security-Policy", "default-src 'self'");
                    httpRes.setHeader("Cross-Origin-Opener-Policy", "same-origin");
                    httpRes.setHeader("Cross-Origin-Resource-Policy", "same-origin");

                    if (httpsEnabled) {
                        httpRes.setHeader("Strict-Transport-Security", "max-age=31536000; includeSubDomains");
                    }
                }
                chain.doFilter(request, response);
            }
        };
    }
}
