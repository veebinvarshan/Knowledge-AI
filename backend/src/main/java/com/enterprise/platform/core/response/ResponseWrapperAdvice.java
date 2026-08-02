package com.enterprise.platform.core.response;

import com.enterprise.platform.core.correlation.RequestContext;
import com.enterprise.platform.core.correlation.RequestContextHolder;
import org.springframework.core.MethodParameter;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

@ControllerAdvice
public class ResponseWrapperAdvice implements ResponseBodyAdvice<Object> {

    private final Clock clock;

    public ResponseWrapperAdvice(Clock clock) {
        this.clock = clock;
    }

    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        Class<?> paramType = returnType.getParameterType();
        if (paramType == void.class || paramType == Void.class) {
            return false;
        }
        if (ApiResponse.class.isAssignableFrom(paramType)
                || Resource.class.isAssignableFrom(paramType)
                || StreamingResponseBody.class.isAssignableFrom(paramType)
                || paramType == byte[].class
                || ProblemDetail.class.isAssignableFrom(paramType)) {
            return false;
        }
        // Jackson converter only
        return MappingJackson2HttpMessageConverter.class.isAssignableFrom(converterType);
    }

    @Override
    public Object beforeBodyWrite(
            Object body,
            MethodParameter returnType,
            MediaType selectedContentType,
            Class<? extends HttpMessageConverter<?>> selectedConverterType,
            ServerHttpRequest request,
            ServerHttpResponse response) {

        if (body instanceof ApiResponse) {
            return body;
        }

        // Bypassing media types like Server-Sent Events or WebSockets
        if (selectedContentType.includes(MediaType.TEXT_EVENT_STREAM)) {
            return body;
        }

        // Path-based exemptions (Actuator, OpenAPI/Swagger docs)
        String path = request.getURI().getPath();
        if (path.startsWith("/actuator") || path.startsWith("/v3/api-docs") || path.startsWith("/swagger-ui")) {
            return body;
        }

        RequestContext ctx = RequestContextHolder.getContext();
        String requestId = (ctx != null) ? ctx.getRequestId() : UUID.randomUUID().toString();

        return new ApiResponse(
                true,
                Instant.now(clock).toString(),
                requestId,
                body
        );
    }
}
