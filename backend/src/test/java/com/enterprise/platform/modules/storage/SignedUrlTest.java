package com.enterprise.platform.modules.storage;

import com.enterprise.platform.modules.storage.service.dto.SignedUrl;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

public class SignedUrlTest {

    @Test
    void testSignedUrlProperties() {
        // GIVEN
        Instant exp = Instant.now().plusSeconds(3600);
        
        // WHEN
        SignedUrl signed = new SignedUrl("https://s3.amazonaws.com/bucket/doc.pdf", exp, "S3", "GET");

        // THEN
        assertEquals("https://s3.amazonaws.com/bucket/doc.pdf", signed.url());
        assertEquals(exp, signed.expiration());
        assertEquals("S3", signed.provider());
        assertEquals("GET", signed.httpMethod());
    }
}
