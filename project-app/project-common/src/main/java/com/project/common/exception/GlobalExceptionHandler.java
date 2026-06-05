package com.project.common.exception;

import com.project.common.model.GenericResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Kendi yazdigimiz is kurallari hatalarini (BusinessException) yakalar. (Orn: Bakiye yetersiz)
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<GenericResponse<Void>> handleBusinessException(BusinessException ex) {
        String traceId = MDC.get("traceId");
        logger.warn("Business rule violation [TraceID: {}] - Code: {}, Message: {}", traceId, ex.getErrorCode(), ex.getMessage());

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

        logger.warn("Validation failed: {}", errorMessage);

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
        // Kritik hatalari ERROR seviyesinde logluyoruz
        logger.error("Unexpected system error occurred [TraceID: {}]", traceId, ex);

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(GenericResponse.error("An unexpected error occurred. Please contact support with Trace ID: " + traceId));
    }
    /**
     * Veritabani "Unique" (Benzersizlik) kısıtlamalarına takılan işlemleri yakalar.
     * Örneğin: Aynı email veya aynı kullanıcı adı ile ikinci kez kayıt olmaya çalışmak.
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<GenericResponse<Void>> handleDataIntegrityViolationException(DataIntegrityViolationException ex) {
        String traceId = MDC.get("traceId");

        // Hatayı konsola logluyoruz (Geliştirici için)
        logger.warn("Data integrity violation [TraceID: {}] - Message: {}", traceId, ex.getMessage());

        // Kullanıcıya tertemiz bir mesaj dönüyoruz (409 Conflict HTTP Kodu ile)
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(GenericResponse.error("Bu kayıt (örn: e-posta adresi) sistemde zaten mevcut. Lütfen farklı bilgilerle tekrar deneyin."));
    }
}