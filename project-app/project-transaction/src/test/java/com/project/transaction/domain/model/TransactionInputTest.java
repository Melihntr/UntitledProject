package com.project.transaction.domain.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TransactionInputTest {

    @Test
    void builder_and_getters_work() {
        TransactionInput input = TransactionInput.builder()
                .senderUserId("alice")
                .receiverUserId("bob")
                .amount(12.5)
                .build();

        assertThat(input).isNotNull();
        assertThat(input.getSenderUserId()).isEqualTo("alice");
        assertThat(input.getReceiverUserId()).isEqualTo("bob");
        assertThat(input.getAmount()).isEqualTo(12.5);
    }

    @Test
    void builder_allowsNulls_whenNotProvided() {
        TransactionInput input = TransactionInput.builder().build();

        assertThat(input).isNotNull();
        assertThat(input.getSenderUserId()).isNull();
        assertThat(input.getReceiverUserId()).isNull();
        assertThat(input.getAmount()).isNull();
    }
}
