package com.enterprise.platform.core.config;

import com.enterprise.platform.core.config.properties.CorsProperties;
import com.enterprise.platform.core.security.SecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class CorsConfigurationTest {

    @Test
    void testCorsConfigurationSourcePropertiesApplied() {
        // GIVEN
        CorsProperties properties = new CorsProperties(
                List.of("https://app.enterprise.com"),
                List.of("GET", "POST"),
                List.of("Authorization"),
                true,
                7200L
        );

        SecurityConfig securityConfig = new SecurityConfig(null, null, null, null, null, properties);

        // WHEN
        CorsConfigurationSource source = securityConfig.corsConfigurationSource();
        assertNotNull(source);

        CorsConfiguration config = source.getCorsConfiguration(
                new org.springframework.mock.web.MockHttpServletRequest("GET", "/api/v1/documents")
        );

        // THEN
        assertNotNull(config);
        assertEquals(List.of("https://app.enterprise.com"), config.getAllowedOrigins());
        assertEquals(List.of("GET", "POST"), config.getAllowedMethods());
        assertEquals(List.of("Authorization"), config.getAllowedHeaders());
        assertTrue(config.getAllowCredentials());
        assertEquals(7200L, config.getMaxAge());
    }
}
