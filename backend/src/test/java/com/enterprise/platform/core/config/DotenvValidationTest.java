package com.enterprise.platform.core.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = DotenvValidationTest.EmptyConfig.class)
@ActiveProfiles("dev")
public class DotenvValidationTest {

    @SpringBootConfiguration
    public static class EmptyConfig {}

    @Autowired
    private Environment environment;

    @Test
    void verifyDotenvPropertiesAreLoaded() {
        // Verify key resolution in spring environment
        String geminiApiKey = environment.getProperty("GEMINI_API_KEY");
        assertNotNull(geminiApiKey, "GEMINI_API_KEY must be resolved in spring environment");
        assertFalse(geminiApiKey.trim().isEmpty(), "GEMINI_API_KEY must not be empty");

        // The loaded variables should exist and be non-empty (avoid asserting specific values since they can be overridden by system environment)
        assertNotNull(environment.getProperty("DB_HOST"));
        assertNotNull(environment.getProperty("DB_PORT"));
        assertNotNull(environment.getProperty("DB_NAME"));
        assertNotNull(environment.getProperty("DB_USER"));
        assertNotNull(environment.getProperty("DB_PASSWORD"));
        assertNotNull(environment.getProperty("REDIS_HOST"));
        assertNotNull(environment.getProperty("REDIS_PORT"));
    }
}
