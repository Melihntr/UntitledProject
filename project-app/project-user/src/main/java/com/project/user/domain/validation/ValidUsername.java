package com.project.user.domain.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

/**
 * Custom validation annotation to ensure the username meets specific business rules.
 * For example: No spaces allowed, must be alphanumeric, etc.
 */
@Documented
@Constraint(validatedBy = UsernameValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidUsername {

    String message() default "Invalid username format. It must not contain spaces.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}