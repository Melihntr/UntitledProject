package com.project.user.domain.model;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class UserCreateInputTest {

    private static ValidatorFactory validatorFactory;
    private static Validator validator;

    @BeforeAll
    static void initValidator() {
        validatorFactory = Validation.buildDefaultValidatorFactory();
        validator = validatorFactory.getValidator();
    }

    @AfterAll
    static void closeValidator() {
        validatorFactory.close();
    }

    @Test
    void builder_and_getters_work() {
        UserCreateInput input = UserCreateInput.builder()
                .id("id-123")
                .username("validUser")
                .email("alice@example.com")
                .rawPassword("StrongP@ssw0rd")
                .build();

        assertThat(input).isNotNull();
        assertThat(input.getId()).isEqualTo("id-123");
        assertThat(input.getUsername()).isEqualTo("validUser");
        assertThat(input.getEmail()).isEqualTo("alice@example.com");
        assertThat(input.getRawPassword()).isEqualTo("StrongP@ssw0rd");
    }

    @Test
    void valid_input_has_no_constraint_violations() {
        UserCreateInput input = new UserCreateInput();
        input.setUsername("validUser");
        input.setEmail("alice@example.com");
        input.setRawPassword("StrongP@ssw0rd");

        Set<ConstraintViolation<UserCreateInput>> violations = validator.validate(input);

        assertThat(violations).isEmpty();
    }

    @Test
    void invalid_email_triggers_constraint_violation() {
        UserCreateInput input = new UserCreateInput();
        input.setUsername("validUser");
        input.setEmail("not-an-email");
        input.setRawPassword("StrongP@ssw0rd");

        Set<ConstraintViolation<UserCreateInput>> violations = validator.validate(input);

        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> "email".equals(v.getPropertyPath().toString()));
    }

    @Test
    void blank_password_triggers_constraint_violation() {
        UserCreateInput input = new UserCreateInput();
        input.setUsername("validUser");
        input.setEmail("alice@example.com");
        input.setRawPassword(""); // blank -> should violate @NotBlank

        Set<ConstraintViolation<UserCreateInput>> violations = validator.validate(input);

        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> "rawPassword".equals(v.getPropertyPath().toString()));
    }
}
