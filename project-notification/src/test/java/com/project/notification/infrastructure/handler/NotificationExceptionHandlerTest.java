package com.project.notification.infrastructure.handler;

import com.project.notification.infrastructure.api.dto.ApiErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.core.MethodParameter;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.http.converter.HttpMessageNotReadableException;

import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class NotificationExceptionHandlerTest {

    private final NotificationExceptionHandler handler = new NotificationExceptionHandler();
    private final HttpServletRequest request = request();

    @AfterEach
    void clearMdc() {
        MDC.clear();
    }

    @Test
    void handleValidationException_returnsTypedBadRequest() throws Exception {
        MDC.put("traceId", "trace-1");
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "request");
        bindingResult.addError(new FieldError("request", "recipientId", "Recipient ID cannot be blank"));
        MethodArgumentNotValidException exception =
                new MethodArgumentNotValidException(dummyParameter(), bindingResult);

        ResponseEntity<ApiErrorResponse> response = handler.handleValidationException(exception, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().errorCode()).isEqualTo("NOTIFICATION_REQUEST_INVALID");
        assertThat(response.getBody().traceId()).isEqualTo("trace-1");
        assertThat(response.getBody().path()).isEqualTo("/api/v1/notifications");
        assertThat(response.getBody().validationErrors())
                .containsEntry("recipientId", "Recipient ID cannot be blank");
        assertThat(response.getBody().timestamp()).isNotNull();
    }

    @Test
    void handleDataAccessException_returnsServiceUnavailable() {
        ResponseEntity<ApiErrorResponse> response = handler.handleDataAccessException(
                new DataIntegrityViolationException("db down"), request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
        assertThat(response.getBody().errorCode()).isEqualTo("NOTIFICATION_PERSISTENCE_UNAVAILABLE");
        assertThat(response.getBody().message()).isEqualTo("Notification could not be stored.");
        assertThat(response.getBody().validationErrors()).isEmpty();
        assertThat(response.getBody().status()).isEqualTo(503);
    }

    @Test
    void handleGeneralException_returnsInternalServerError() {
        ResponseEntity<ApiErrorResponse> response =
                handler.handleGeneralException(new RuntimeException("boom"), request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody().errorCode()).isEqualTo("NOTIFICATION_UNEXPECTED_ERROR");
    }

    @Test
    void handleUnreadableRequest_returnsBadRequest() {
        HttpMessageNotReadableException exception = mock(HttpMessageNotReadableException.class);
        when(exception.getMessage()).thenReturn("invalid enum value");

        ResponseEntity<ApiErrorResponse> response = handler.handleUnreadableRequest(
                exception, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().errorCode()).isEqualTo("NOTIFICATION_REQUEST_UNREADABLE");
        assertThat(response.getBody().message()).contains("malformed");
    }

    private HttpServletRequest request() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn("/api/v1/notifications");
        return request;
    }

    private MethodParameter dummyParameter() throws NoSuchMethodException {
        Method method = NotificationExceptionHandlerTest.class.getDeclaredMethod("dummy", String.class);
        return new MethodParameter(method, 0);
    }

    @SuppressWarnings("unused")
    private void dummy(String value) {
    }
}
