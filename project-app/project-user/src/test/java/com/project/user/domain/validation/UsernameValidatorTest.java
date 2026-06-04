package com.project.user.domain.validation;

import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class UsernameValidatorTest {

    private final UsernameValidator validator = new UsernameValidator();

    @Mock
    private ConstraintValidatorContext context;

    @Test
    void isValid_returnsFalse_whenValueIsNull() {
        boolean result = validator.isValid(null, context);
        assertThat(result).isFalse();
    }

    @Test
    void isValid_returnsFalse_whenValueIsEmptyAfterTrim() {
        boolean result = validator.isValid("   ", context);
        assertThat(result).isFalse();
    }

    @Test
    void isValid_returnsFalse_whenValueContainsInternalWhitespace() {
        boolean result = validator.isValid("alice bob", context);
        assertThat(result).isFalse();
    }

    @Test
    void isValid_returnsTrue_whenValueHasNoWhitespace() {
        boolean result = validator.isValid("alice", context);
        assertThat(result).isTrue();
    }

    @Test
    void isValid_trimsAndAccepts_whenLeadingOrTrailingSpacesOnly() {
        // Leading/trailing spaces should be trimmed and accepted if inner content has no spaces
        boolean result = validator.isValid("  alice  ", context);
        assertThat(result).isTrue();
    }
}