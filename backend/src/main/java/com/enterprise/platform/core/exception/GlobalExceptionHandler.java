package com.enterprise.platform.core.exception;

import com.enterprise.platform.core.correlation.RequestContext;
import com.enterprise.platform.core.correlation.RequestContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import java.time.Clock;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@ControllerAdvice
public class GlobalExceptionHandler {

    private final Clock clock;

    public GlobalExceptionHandler(Clock clock) {
        this.clock = clock;
    }

    @ExceptionHandler(PlatformException.class)
    public ResponseEntity<Map<String, Object>> handlePlatformException(PlatformException ex, WebRequest request) {
        HttpStatus status = ex.getHttpStatus();
        Map<String, Object> body = createErrorBody(status, ex.getErrorCode().name(), ex.getMessage(), request);
        return new ResponseEntity<>(body, status);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationExceptions(MethodArgumentNotValidException ex, WebRequest request) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        Map<String, Object> body = createErrorBody(status, ErrorCode.VALIDATION_FAILED.name(), "Validation failed", request);

        List<Map<String, Object>> validationErrors = ex.getBindingResult().getFieldErrors().stream()
                .map(this::mapFieldError)
                .collect(Collectors.toList());
        body.put("validationErrors", validationErrors);

        return new ResponseEntity<>(body, status);
    }

    @ExceptionHandler(com.enterprise.platform.modules.authentication.domain.InvalidCredentialsException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidCredentialsException(
            com.enterprise.platform.modules.authentication.domain.InvalidCredentialsException ex, WebRequest request) {
        HttpStatus status = HttpStatus.UNAUTHORIZED;
        Map<String, Object> body = createErrorBody(status, ErrorCode.ACCESS_DENIED.name(), ex.getMessage(), request);
        return new ResponseEntity<>(body, status);
    }

    @ExceptionHandler(com.enterprise.platform.modules.authentication.domain.InvalidTokenException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidTokenException(
            com.enterprise.platform.modules.authentication.domain.InvalidTokenException ex, WebRequest request) {
        HttpStatus status = HttpStatus.UNAUTHORIZED;
        Map<String, Object> body = createErrorBody(status, ErrorCode.AUTHENTICATION_REQUIRED.name(), ex.getMessage(), request);
        return new ResponseEntity<>(body, status);
    }

    @ExceptionHandler(com.enterprise.platform.modules.authentication.domain.AccountLockedException.class)
    public ResponseEntity<Map<String, Object>> handleAccountLockedException(
            com.enterprise.platform.modules.authentication.domain.AccountLockedException ex, WebRequest request) {
        HttpStatus status = HttpStatus.FORBIDDEN;
        Map<String, Object> body = createErrorBody(status, ErrorCode.ACCESS_DENIED.name(), ex.getMessage(), request);
        return new ResponseEntity<>(body, status);
    }

    @ExceptionHandler(com.enterprise.platform.modules.authentication.domain.AuthenticationException.class)
    public ResponseEntity<Map<String, Object>> handleAuthenticationException(
            com.enterprise.platform.modules.authentication.domain.AuthenticationException ex, WebRequest request) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        if ("Identity already registered under this organization".equals(ex.getMessage())) {
            status = HttpStatus.CONFLICT;
        } else if ("Email verification is pending".equals(ex.getMessage())) {
            status = HttpStatus.UNAUTHORIZED;
        }
        Map<String, Object> body = createErrorBody(status, ErrorCode.ACCESS_DENIED.name(), ex.getMessage(), request);
        return new ResponseEntity<>(body, status);
    }

    @ExceptionHandler(org.springframework.security.access.AccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> handleAccessDeniedException(
            org.springframework.security.access.AccessDeniedException ex, WebRequest request) {
        HttpStatus status = HttpStatus.FORBIDDEN;
        Map<String, Object> body = createErrorBody(status, ErrorCode.ACCESS_DENIED.name(), ex.getMessage(), request);
        return new ResponseEntity<>(body, status);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleAllExceptions(Exception ex, WebRequest request) {
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        Map<String, Object> body = createErrorBody(status, ErrorCode.INTERNAL_SERVER_ERROR.name(), ex.getMessage(), request);
        return new ResponseEntity<>(body, status);
    }

    private Map<String, Object> createErrorBody(HttpStatus status, String errorCode, String message, WebRequest request) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("success", false);
        body.put("timestamp", Instant.now(clock).toString());
        body.put("status", status.value());
        body.put("error", errorCode);
        body.put("message", message);
        body.put("path", request.getDescription(false).replace("uri=", ""));
        
        RequestContext context = RequestContextHolder.getContext();
        body.put("requestId", context != null ? context.getRequestId() : UUID.randomUUID().toString());
        body.put("validationErrors", Collections.emptyList());
        return body;
    }

    private Map<String, Object> mapFieldError(FieldError error) {
        Map<String, Object> errMap = new LinkedHashMap<>();
        errMap.put("field", error.getField());
        
        Object rejected = error.getRejectedValue();
        errMap.put("rejectedValue", maskSensitiveValue(error.getField(), rejected));
        errMap.put("message", error.getDefaultMessage());
        return errMap;
    }

    private Object maskSensitiveValue(String field, Object value) {
        if (value == null) {
            return null;
        }
        String lowerField = field.toLowerCase();
        if (lowerField.contains("password") || lowerField.contains("token") || lowerField.contains("secret") || lowerField.contains("key")) {
            return "[MASKED]";
        }
        return value;
    }
}
