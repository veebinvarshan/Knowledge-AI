package com.enterprise.platform.core.response;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;

import java.net.URI;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ResponseWrapperTest {

    private ResponseWrapperAdvice wrapper;
    private Clock clock;
    private ServerHttpRequest request;
    private ServerHttpResponse response;

    @BeforeEach
    void setUp() {
        clock = Clock.fixed(Instant.parse("2026-06-27T08:30:00Z"), ZoneId.of("UTC"));
        wrapper = new ResponseWrapperAdvice(clock);
        request = mock(ServerHttpRequest.class);
        response = mock(ServerHttpResponse.class);
    }

    @Test
    void testSupportsStandardJacksonConverterOnly() {
        // GIVEN
        MethodParameter parameter = mock(MethodParameter.class);
        when(parameter.getParameterType()).thenAnswer(invocation -> String.class);

        Class<? extends HttpMessageConverter<?>> jacksonClass = MappingJackson2HttpMessageConverter.class;

        // WHEN / THEN
        assertTrue(wrapper.supports(parameter, jacksonClass));
    }

    @Test
    void testSupportsExcludesResourceAndByteArrays() {
        // GIVEN
        MethodParameter paramResource = mock(MethodParameter.class);
        when(paramResource.getParameterType()).thenAnswer(invocation -> org.springframework.core.io.Resource.class);

        MethodParameter paramBytes = mock(MethodParameter.class);
        when(paramBytes.getParameterType()).thenAnswer(invocation -> byte[].class);

        Class<? extends HttpMessageConverter<?>> jacksonClass = MappingJackson2HttpMessageConverter.class;

        // WHEN / THEN
        assertFalse(wrapper.supports(paramResource, jacksonClass));
        assertFalse(wrapper.supports(paramBytes, jacksonClass));
    }

    @Test
    void testWrapPayloadCorrectly() throws Exception {
        // GIVEN
        Object originalBody = Map.of("id", "123", "name", "DocumentName");
        when(request.getURI()).thenReturn(new URI("http://localhost:8080/api/v1/documents"));

        // WHEN
        Object result = wrapper.beforeBodyWrite(
                originalBody, null, MediaType.APPLICATION_JSON, null, request, response
        );

        // THEN
        assertTrue(result instanceof ApiResponse);
        ApiResponse wrapped = (ApiResponse) result;
        assertTrue(wrapped.success());
        assertEquals("2026-06-27T08:30:00Z", wrapped.timestamp());
        assertEquals(originalBody, wrapped.data());
    }

    @Test
    void testResponseWrapperIdempotency() throws Exception {
        // GIVEN
        ApiResponse doubleWrapCandidate = new ApiResponse(true, "2026-06-27T08:30:00Z", "trace-id", "some data");
        when(request.getURI()).thenReturn(new URI("http://localhost:8080/api/v1/documents"));

        // WHEN
        Object result = wrapper.beforeBodyWrite(
                doubleWrapCandidate, null, MediaType.APPLICATION_JSON, null, request, response
        );

        // THEN (Returns unchanged candidate directly without wrapping it again)
        assertSame(doubleWrapCandidate, result);
    }

    @Test
    void testActuatorExemptionsBypassWrapping() throws Exception {
        // GIVEN
        Object healthBody = Map.of("status", "UP");
        when(request.getURI()).thenReturn(new URI("http://localhost:8080/actuator/health"));

        // WHEN
        Object result = wrapper.beforeBodyWrite(
                healthBody, null, MediaType.APPLICATION_JSON, null, request, response
        );

        // THEN (Actuator paths must bypass autowrapping)
        assertSame(healthBody, result);
    }
}

// Simple Map replacement helper for the test above
class Map {
    static java.util.Map<String, Object> of(String k1, Object v1) {
        java.util.Map<String, Object> m = new java.util.HashMap<>();
        m.put(k1, v1);
        return m;
    }
    static java.util.Map<String, Object> of(String k1, Object v1, String k2, Object v2) {
        java.util.Map<String, Object> m = new java.util.HashMap<>();
        m.put(k1, v1);
        m.put(k2, v2);
        return m;
    }
}
