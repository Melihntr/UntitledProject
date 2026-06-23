package com.project.common.domain.exception;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AuthExceptionTest {

    @Test
    void authenticationFailedExceptionKeepsMessage() {
        AuthenticationFailedException exception = new AuthenticationFailedException("bad login");

        assertThat(exception).hasMessage("bad login");
    }

    @Test
    void invalidTokenExceptionKeepsMessage() {
        InvalidTokenException exception = new InvalidTokenException("bad token");

        assertThat(exception).hasMessage("bad token");
    }
}
