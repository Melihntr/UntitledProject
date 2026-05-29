package com.project.user.domain.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * Validator implementation for @ValidUsername annotation.
 */
public class UsernameValidator implements ConstraintValidator<ValidUsername, String> {

    @Override
    public void initialize(ValidUsername constraintAnnotation) {
        // Initialization logic if needed
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        // Business rule: Username cannot be null and must not contain whitespace
        if (value == null || value.trim().isEmpty()) {
            return false;
        }
        return !value.contains(" ");
    }
}