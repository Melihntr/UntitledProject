package com.project.common.infrastructure.handler;

import com.project.common.domain.exception.AccessDeniedException;
import com.project.common.domain.exception.AuthenticationFailedException;
import com.project.common.domain.exception.BusinessException;
import com.project.common.domain.exception.InvalidTokenException;
import com.project.common.domain.exception.ResourceNotFoundException;
import com.project.common.infrastructure.model.GenericResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.core.MethodParameter;

import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @AfterEach
    void cleanup() {
        MDC.clear();
    }

    @Test
    void handleBusinessException_returnsBadRequest() {
        MDC.put("traceId", "trace-1");

        ResponseEntity<GenericResponse<Void>> response =
                handler.handleBusinessException(new BusinessException("ERR", "bad business"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isFalse();
        assertThat(response.getBody().getMessage()).isEqualTo("bad business");
    }

    @Test
    void handleAccessDeniedException_returnsForbidden() {
        MDC.put("traceId", "trace-forbidden");

        ResponseEntity<GenericResponse<Void>> response =
                handler.handleAccessDeniedException(new AccessDeniedException("Wallet access denied"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isFalse();
        assertThat(response.getBody().getMessage()).isEqualTo("Wallet access denied");
    }

    @Test
    void handleAuthenticationFailedException_returnsUnauthorized() {
        MDC.put("traceId", "trace-auth");

        ResponseEntity<GenericResponse<Void>> response =
                handler.handleAuthenticationFailedException(new AuthenticationFailedException("Invalid username or password."));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).isEqualTo("Invalid username or password.");
    }

    @Test
    void handleInvalidTokenException_returnsUnauthorized() {
        MDC.put("traceId", "trace-token");

        ResponseEntity<GenericResponse<Void>> response =
                handler.handleInvalidTokenException(new InvalidTokenException("Token has expired."));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).isEqualTo("Token has expired.");
    }

    @Test
    void handleResourceNotFoundException_returnsNotFound() {
        MDC.put("traceId", "trace-not-found");

        ResponseEntity<GenericResponse<Void>> response =
                handler.handleResourceNotFoundException(new ResourceNotFoundException("User not found"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isFalse();
        assertThat(response.getBody().getMessage()).isEqualTo("User not found");
    }

    @Test
    void handleValidationException_returnsJoinedValidationMessages() throws Exception {
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "request");
        bindingResult.addError(new FieldError("request", "name", "Name cannot be blank"));
        MethodArgumentNotValidException exception = new MethodArgumentNotValidException(dummyParameter(), bindingResult);

        ResponseEntity<GenericResponse<Void>> response = handler.handleValidationException(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).contains("Name cannot be blank");
    }

    @Test
    void handleGeneralException_returnsTraceAwareMessage() {
        MDC.put("traceId", "trace-2");

        ResponseEntity<GenericResponse<Void>> response = handler.handleGeneralException(new RuntimeException("boom"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).contains("trace-2");
    }

    @Test
    void handleDataIntegrityViolationException_returnsConflict() {
        MDC.put("traceId", "trace-3");

        ResponseEntity<GenericResponse<Void>> response =
                handler.handleDataIntegrityViolationException(new DataIntegrityViolationException("duplicate"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).contains("sistemde zaten mevcut");
    }

    private MethodParameter dummyParameter() throws NoSuchMethodException {
        Method method = GlobalExceptionHandlerTest.class.getDeclaredMethod("dummy", String.class);
        return new MethodParameter(method, 0);
    }

    @SuppressWarnings("unused")
    private void dummy(String value) {
    }
}
