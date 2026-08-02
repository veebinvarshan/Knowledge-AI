package com.enterprise.platform.core.exception;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.context.request.WebRequest;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;
    private Clock clock;
    private WebRequest webRequest;

    @BeforeEach
    void setUp() {
        clock = Clock.fixed(Instant.parse("2026-06-27T08:30:00Z"), ZoneId.of("UTC"));
        handler = new GlobalExceptionHandler(clock);
        webRequest = mock(WebRequest.class);
        when(webRequest.getDescription(false)).thenReturn("uri=/test-endpoint");
    }

    @Test
    void testPlatformExceptionMapping() {
        // GIVEN
        PlatformException ex = new ResourceNotFoundException("Document not found");

        // WHEN
        ResponseEntity<Map<String, Object>> response = handler.handlePlatformException(ex, webRequest);

        // THEN
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertEquals(false, body.get("success"));
        assertEquals("2026-06-27T08:30:00Z", body.get("timestamp"));
        assertEquals(404, body.get("status"));
        assertEquals("DOCUMENT_NOT_FOUND", body.get("error"));
        assertEquals("Document not found", body.get("message"));
        assertEquals("/test-endpoint", body.get("path"));
    }

    @Test
    void testValidationExceptionMapping() {
        // GIVEN
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError fieldError = new FieldError("user", "password", "secret123", false, null, null, "Password too weak");
        when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError));

        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(null, bindingResult);

        // WHEN
        ResponseEntity<Map<String, Object>> response = handler.handleValidationExceptions(ex, webRequest);

        // THEN
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertEquals("VALIDATION_FAILED", body.get("error"));

        List<Map<String, Object>> validationErrors = (List<Map<String, Object>>) body.get("validationErrors");
        assertEquals(1, validationErrors.size());
        Map<String, Object> errorMap = validationErrors.get(0);
        assertEquals("password", errorMap.get("field"));
        assertEquals("[MASKED]", errorMap.get("rejectedValue")); // sensitive value is masked
        assertEquals("Password too weak", errorMap.get("message"));
    }

    @Test
    void testFallbackExceptionMapping() {
        // GIVEN
        Exception ex = new RuntimeException("Unexpected panic error");

        // WHEN
        ResponseEntity<Map<String, Object>> response = handler.handleAllExceptions(ex, webRequest);

        // THEN
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertEquals("INTERNAL_SERVER_ERROR", body.get("error"));
        assertEquals("Unexpected panic error", body.get("message"));
    }
}
