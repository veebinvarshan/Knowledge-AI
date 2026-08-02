package com.enterprise.platform.core.config;

import com.enterprise.platform.core.config.properties.JwtProperties;
import com.enterprise.platform.core.config.properties.RedisProperties;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class ConfigurationValidationTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void testJwtPropertiesValidContext() {
        // GIVEN
        JwtProperties properties = new JwtProperties("long-secret-key-123456789-abc", 3600);

        // WHEN
        Set<ConstraintViolation<JwtProperties>> violations = validator.validate(properties);

        // THEN
        assertTrue(violations.isEmpty());
    }

    @Test
    void testJwtPropertiesInvalidSecretFails() {
        // GIVEN (Blank secret)
        JwtProperties properties = new JwtProperties("", 3600);

        // WHEN
        Set<ConstraintViolation<JwtProperties>> violations = validator.validate(properties);

        // THEN
        assertFalse(violations.isEmpty());
    }

    @Test
    void testRedisPropertiesInvalidPortFails() {
        // GIVEN (Port out of bounds)
        RedisProperties properties = new RedisProperties("localhost", 999999, null, 0, 500);

        // WHEN
        Set<ConstraintViolation<RedisProperties>> violations = validator.validate(properties);

        // THEN
        assertFalse(violations.isEmpty());
    }

    @Test
    void testStoragePropertiesValidation() {
        // GIVEN (Blank provider)
        com.enterprise.platform.core.config.properties.StorageProperties properties = 
                new com.enterprise.platform.core.config.properties.StorageProperties("", 1000L);

        // WHEN
        Set<ConstraintViolation<com.enterprise.platform.core.config.properties.StorageProperties>> violations = validator.validate(properties);

        // THEN
        assertFalse(violations.isEmpty());
    }

    @Test
    void testLocalStoragePropertiesValidation() {
        // GIVEN (Blank root directory)
        com.enterprise.platform.core.config.properties.LocalStorageProperties properties = 
                new com.enterprise.platform.core.config.properties.LocalStorageProperties("");

        // WHEN
        Set<ConstraintViolation<com.enterprise.platform.core.config.properties.LocalStorageProperties>> violations = validator.validate(properties);

        // THEN
        assertFalse(violations.isEmpty());
    }
}
