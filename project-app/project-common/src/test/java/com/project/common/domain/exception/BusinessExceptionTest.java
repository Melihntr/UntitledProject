package com.project.common.domain.exception;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BusinessExceptionTest {

    @Test
    void singleArgumentConstructor_usesDefaultErrorCode() {
        BusinessException exception = new BusinessException("Balance is not enough");

        assertThat(exception.getMessage()).isEqualTo("Balance is not enough");
        assertThat(exception.getErrorCode()).isEqualTo("BUSINESS_ERROR");
    }

    @Test
    void twoArgumentConstructor_usesProvidedErrorCode() {
        BusinessException exception = new BusinessException("WALLET_NOT_FOUND", "Wallet missing");

        assertThat(exception.getMessage()).isEqualTo("Wallet missing");
        assertThat(exception.getErrorCode()).isEqualTo("WALLET_NOT_FOUND");
    }
}
