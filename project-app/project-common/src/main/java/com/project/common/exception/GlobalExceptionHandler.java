package com.project.common.exception;

import com.project.common.model.GenericResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Intercepts all exceptions globally across the application.
 * Formats these exceptions into a standardized API response structure,
 * ensuring a consistent error contract for external clients.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handles specific domain and business rule violations.
     * For example: Insufficient balance, invalid state, or domain validation failures.
     *
     * @param ex The thrown BusinessException containing the specific error detail.
     * @return A standardized API response mapped to HTTP 400 (Bad Request).
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<GenericResponse<String>> handleBusinessException(BusinessException ex) {
        // Note: If a specific error builder (e.g., GenericResponse.error()) is implemented 
        // in your generic response structure, it is highly recommended to use it here.
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(GenericResponse.success("ERROR: " + ex.getMessage())); 
    }

    /**
     * Acts as a fallback mechanism for any uncaught or unexpected system exceptions.
     * Ensures that the application does not crash or leak sensitive stack traces to the client.
     *
     * @param ex The generic Exception thrown during runtime.
     * @return A standardized API response mapped to HTTP 500 (Internal Server Error).
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<GenericResponse<String>> handleGeneralException(Exception ex) {
        // Handles unexpected system failures, database connection losses, or unhandled null pointers
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(GenericResponse.success("SYSTEM ERROR: " + ex.getMessage()));
    }
}