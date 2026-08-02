package com.enterprise.platform.modules.embedding;

import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

public class RetryPolicyTest {

    @Test
    void testIsRetryableException() {
        Exception e1 = new IOException("Connection timed out");
        Exception e2 = new IllegalArgumentException("Invalid key format");

        assertTrue(isRetryable(e1));
        assertFalse(isRetryable(e2));
    }

    private boolean isRetryable(Throwable t) {
        String msg = t.getMessage() != null ? t.getMessage().toLowerCase() : "";
        return msg.contains("timeout") || msg.contains("timed out") || msg.contains("temporary") || msg.contains("network") || msg.contains("connection refused");
    }
}
