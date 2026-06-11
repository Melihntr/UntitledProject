package com.project.common.handler;

import com.project.common.exception.AccessDeniedException;
import com.project.common.exception.BusinessException;
import com.project.common.exception.ResourceNotFoundException;
import com.project.common.model.GenericResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<GenericResponse<Void>> handleAccessDeniedException(AccessDeniedException ex) {
        String traceId = MDC.get("traceId");
        log.warn("request.access-denied traceId={} message={}", traceId, ex.getMessage());

        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(GenericResponse.error(ex.getMessage()));
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<GenericResponse<Void>> handleResourceNotFoundException(ResourceNotFoundException ex) {
        String traceId = MDC.get("traceId");
        log.warn("request.resource-not-found traceId={} message={}", traceId, ex.getMessage());

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(GenericResponse.error(ex.getMessage()));
    }

    /**
     * Kendi yazdigimiz is kurallari hatalarini (BusinessException) yakalar. (Orn: Bakiye yetersiz)
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<GenericResponse<Void>> handleBusinessException(BusinessException ex) {
        String traceId = MDC.get("traceId");
        log.warn("request.business-rule-violation traceId={} errorCode={} message={}",
                traceId, ex.getErrorCode(), ex.getMessage());

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(GenericResponse.error(ex.getMessage()));
    }

    /**
     * @Valid anotasyonuna takilan (Orn: Bos username, hatali email) validasyon hatalarini yakalar.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<GenericResponse<Void>> handleValidationException(MethodArgumentNotValidException ex) {
        String errorMessage = ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));

        log.warn("request.validation-failed message={}", errorMessage);

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(GenericResponse.error("Validation failed: " + errorMessage));
    }

    /**
     * Ongorulemeyen (NullPointer, SQL hatalari vb.) tum kritik hatalari yakalar.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<GenericResponse<Void>> handleGeneralException(Exception ex) {
        String traceId = MDC.get("traceId");
        log.error("request.unexpected-failure traceId={}", traceId, ex);

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(GenericResponse.error("An unexpected error occurred. Please contact support with Trace ID: " + traceId));
    }

    /**
     * Veritabani "Unique" (Benzersizlik) kisitlamalarina takilan islemleri yakalar.
     * Ornegin: Ayni email veya ayni kullanici adi ile ikinci kez kayit olmaya calismak.
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<GenericResponse<Void>> handleDataIntegrityViolationException(DataIntegrityViolationException ex) {
        String traceId = MDC.get("traceId");

        log.warn("request.data-integrity-violation traceId={} message={}", traceId, ex.getMessage());

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(GenericResponse.error("Bu kayit (orn: e-posta adresi) sistemde zaten mevcut. Lutfen farkli bilgilerle tekrar deneyin."));
    }
}
