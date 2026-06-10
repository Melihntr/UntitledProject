package com.project.user.domain.validation;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UsernameValidatorTest {

    private final UsernameValidator validator = new UsernameValidator();

    @Test
    void initialize_acceptsConstraintMetadata() {
        validator.initialize(null);
    }

    @Test
    void isValid_rejectsNullBlankAndWhitespace() {
        assertThat(validator.isValid(null, null)).isFalse();
        assertThat(validator.isValid(" ", null)).isFalse();
        assertThat(validator.isValid("bad user", null)).isFalse();
    }

    @Test
    void isValid_acceptsNonBlankUsernameWithoutSpaces() {
        assertThat(validator.isValid("good_user", null)).isTrue();
    }
}
