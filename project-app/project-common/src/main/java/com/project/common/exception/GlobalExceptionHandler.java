package com.project.common.exception;

import com.project.common.model.GenericResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Intercepts all exceptions globally and formats them into standard API responses.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<GenericResponse<String>> handleBusinessException(BusinessException ex) {
        // Özel iş kuralları hataları (Örn: Yetersiz bakiye)
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(GenericResponse.success("ERROR: " + ex.getMessage())); 
                // Not: Kendi yapınıza göre GenericResponse.error() metodunuz varsa onu kullanın
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<GenericResponse<String>> handleGeneralException(Exception ex) {
        // Beklenmeyen sistem hataları
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(GenericResponse.success("SYSTEM ERROR: " + ex.getMessage()));
    }
}