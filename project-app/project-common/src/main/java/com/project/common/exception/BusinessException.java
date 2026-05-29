package com.project.common.exception;

/**
 * Custom exception class for handling domain-specific business rule violations.
 * Extending {@link RuntimeException} ensures that when this exception is thrown,
 * the active database transaction can be automatically rolled back by the framework.
 */
public class BusinessException extends RuntimeException {

    /**
     * Constructs a new BusinessException with the specified detail message.
     *
     * @param message The detail message explaining the specific business rule violation.
     */
    public BusinessException(String message) {
        super(message);
    }
}