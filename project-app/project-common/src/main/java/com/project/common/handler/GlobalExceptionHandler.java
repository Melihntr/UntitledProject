package com.project.common.handler;

import com.project.common.exception.BusinessException;
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

    /**
     * Kendi yazdigimiz is kurallari hatalarini (BusinessException) yakalar. (Orn: Bakiye yetersiz)
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<GenericResponse<Void>> handleBusinessException(BusinessException ex) {
        String traceId = MDC.get("traceId");
        log.warn("Business rule violation [TraceID: {}] - Code: {}, Message: {}", traceId, ex.getErrorCode(), ex.getMessage());

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

        log.warn("Validation failed: {}", errorMessage);

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
        log.error("Unexpected system error occurred [TraceID: {}]", traceId, ex);

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

        log.warn("Data integrity violation [TraceID: {}] - Message: {}", traceId, ex.getMessage());

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(GenericResponse.error("Bu kayit (orn: e-posta adresi) sistemde zaten mevcut. Lutfen farkli bilgilerle tekrar deneyin."));
    }
}
