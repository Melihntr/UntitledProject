package com.project.user.domain.validation;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class ValidUsernameAnnotationIntegrationTest {

    private static ValidatorFactory validatorFactory;
    private static Validator validator;

    // Small test bean that uses the annotation on a field named 'username'
    static class Bean {
        @ValidUsername
        String username;

        Bean(String username) {
            this.username = username;
        }
    }

    @BeforeAll
    static void setup() {
        validatorFactory = Validation.buildDefaultValidatorFactory();
        validator = validatorFactory.getValidator();
    }

    @AfterAll
    static void tearDown() {
        validatorFactory.close();
    }

    @Test
    void annotation_triggersViolation_forNullAndBlank() {
        Bean nullBean = new Bean(null);
        Set<ConstraintViolation<Bean>> v1 = validator.validate(nullBean);
        assertThat(v1).isNotEmpty();
        assertThat(v1).anyMatch(v -> "username".equals(v.getPropertyPath().toString()));

        Bean blankBean = new Bean("   ");
        Set<ConstraintViolation<Bean>> v2 = validator.validate(blankBean);
        assertThat(v2).isNotEmpty();
        assertThat(v2).anyMatch(v -> "username".equals(v.getPropertyPath().toString()));
    }

    @Test
    void annotation_triggersViolation_whenContainsInternalSpace() {
        Bean bean = new Bean("alice bob");
        Set<ConstraintViolation<Bean>> violations = validator.validate(bean);
        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> "username".equals(v.getPropertyPath().toString()));
    }

    @Test
    void annotation_allows_value_with_onlyLeadingOrTrailingSpaces() {
        Bean bean = new Bean("  alice  ");
        Set<ConstraintViolation<Bean>> violations = validator.validate(bean);
        assertThat(violations).isEmpty();
    }

    @Test
    void annotation_allows_valid_value() {
        Bean bean = new Bean("alice");
        Set<ConstraintViolation<Bean>> violations = validator.validate(bean);
        assertThat(violations).isEmpty();
    }
}