package com.project.transaction.api.security;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TransactionAccessValidatorTest {

    private final TransactionAccessValidator validator = new TransactionAccessValidator();

    @Test
    void validateSender_allowsMatchingUser() {
        assertThatCode(() -> validator.validateSender("alice", "alice")).doesNotThrowAnyException();
    }

    @Test
    void validateSender_rejectsDifferentUser() {
        assertThatThrownBy(() -> validator.validateSender("alice", "bob"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("own wallet");
    }

    @Test
    void validateHistoryOwner_allowsMatchingUser() {
        assertThatCode(() -> validator.validateHistoryOwner("alice", "alice")).doesNotThrowAnyException();
    }

    @Test
    void validateHistoryOwner_rejectsDifferentUser() {
        assertThatThrownBy(() -> validator.validateHistoryOwner("alice", "bob"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("not authorized");
    }

    @Test
    void validateWalletOwner_allowsMatchingUser() {
        assertThatCode(() -> validator.validateWalletOwner("alice", "alice")).doesNotThrowAnyException();
    }

    @Test
    void validateWalletOwner_rejectsDifferentUser() {
        assertThatThrownBy(() -> validator.validateWalletOwner("alice", "bob"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("own wallet");
    }

    @Test
    void validateAdminRole_allowsAdminCaseInsensitive() {
        assertThatCode(() -> validator.validateAdminRole("admin")).doesNotThrowAnyException();
    }

    @Test
    void validateAdminRole_rejectsNonAdmin() {
        assertThatThrownBy(() -> validator.validateAdminRole("USER"))
                .isInstanceOf(SecurityException.class)
                .hasMessageContaining("Only administrators");
    }
}
