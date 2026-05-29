package com.project.user.domain.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * Validator implementation for the {@link ValidUsername} custom constraint.
 * This validator enforces core domain invariants regarding username formatting.
 * By residing in the domain layer, it ensures that business-critical naming rules 
 * are applied consistently regardless of whether the input originated from a REST 
 * endpoint, a message queue, or an internal administrative task.
 */
public class UsernameValidator implements ConstraintValidator<ValidUsername, String> {

    @Override
    public void initialize(ValidUsername constraintAnnotation) {
        // No specific initialization required for this stateless validator.
    }

    /**
     * Executes the domain-specific validation logic for the username.
     * Rules:
     * 1. Must not be null or empty (after trimming).
     * 2. Must not contain any whitespace characters.
     *
     * @param value   The raw username string to be validated.
     * @param context The validation context.
     * @return true if the username adheres to domain rules, false otherwise.
     */
    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        // Null-safety check
        if (value == null) {
            return false;
        }

        // Business Rule: Username must contain meaningful content and no whitespace.
        // Enterprise Note: This logic can be easily extended to include regex 
        // constraints (e.g., alphanumeric only) or lookups against a blacklist 
        // of reserved/offensive terms.
        String trimmedValue = value.trim();
        
        return !trimmedValue.isEmpty() && !trimmedValue.contains(" ");
    }
}