package com.project.common.exception;

import lombok.Getter;

/**
 * Custom exception class for handling domain-specific business rule violations.
 * Extending {@link RuntimeException} ensures that when this exception is thrown,
 * the active database transaction can be automatically rolled back by the framework.
 */
@Getter
public class BusinessException extends RuntimeException {

    /**
     * Constructs a new BusinessException with the specified detail message.
     *
     * @param message The detail message explaining the specific business rule violation.
     */
        private final String errorCode;

        public BusinessException(String message) {
            super(message);
            this.errorCode = "BUSINESS_ERROR";
        }

        public BusinessException(String errorCode, String message) {
            super(message);
            this.errorCode = errorCode;
        }
    }