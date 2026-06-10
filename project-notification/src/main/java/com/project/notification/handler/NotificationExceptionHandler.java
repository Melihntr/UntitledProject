package com.project.notification.handler;

import com.project.notification.dto.ApiErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.http.converter.HttpMessageNotReadableException;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class NotificationExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidationException(
            MethodArgumentNotValidException exception, HttpServletRequest request) {
        Map<String, String> validationErrors = new LinkedHashMap<>();
        for (FieldError fieldError : exception.getBindingResult().getFieldErrors()) {
            validationErrors.put(fieldError.getField(), fieldError.getDefaultMessage());
        }
        log.warn("notification.request.invalid path={} errors={}", request.getRequestURI(), validationErrors);
        return error(HttpStatus.BAD_REQUEST, "NOTIFICATION_REQUEST_INVALID",
                "Notification request validation failed.", request, validationErrors);
    }

    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<ApiErrorResponse> handleDataAccessException(
            DataAccessException exception, HttpServletRequest request) {
        log.error("notification.persistence.failure path={}", request.getRequestURI(), exception);
        return error(HttpStatus.SERVICE_UNAVAILABLE, "NOTIFICATION_PERSISTENCE_UNAVAILABLE",
                "Notification could not be stored.", request, Map.of());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiErrorResponse> handleUnreadableRequest(
            HttpMessageNotReadableException exception, HttpServletRequest request) {
        log.warn("notification.request.unreadable path={} reason={}", request.getRequestURI(), exception.getMessage());
        return error(HttpStatus.BAD_REQUEST, "NOTIFICATION_REQUEST_UNREADABLE",
                "Notification request body is malformed or contains unsupported values.", request, Map.of());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleGeneralException(Exception exception, HttpServletRequest request) {
        log.error("notification.unexpected.failure path={}", request.getRequestURI(), exception);
        return error(HttpStatus.INTERNAL_SERVER_ERROR, "NOTIFICATION_UNEXPECTED_ERROR",
                "Unexpected notification service error.", request, Map.of());
    }

    private ResponseEntity<ApiErrorResponse> error(
            HttpStatus status,
            String errorCode,
            String message,
            HttpServletRequest request,
            Map<String, String> validationErrors) {
        ApiErrorResponse response = new ApiErrorResponse(
                LocalDateTime.now(),
                status.value(),
                errorCode,
                message,
                MDC.get("traceId"),
                request.getRequestURI(),
                validationErrors
        );
        return ResponseEntity.status(status).body(response);
    }
}
