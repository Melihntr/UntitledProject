package com.project.transaction.domain.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class TransactionRecordModelTest {

    @Test
    void builder_and_getters_work() {
        LocalDateTime now = LocalDateTime.now();

        TransactionRecordModel model = TransactionRecordModel.builder()
                .id("tx-123")
                .senderUserId("alice")
                .receiverUserId("bob")
                .amount(42.5)
                .transactionDate(now)
                .status("COMPLETED")
                .build();

        assertThat(model).isNotNull();
        assertThat(model.getId()).isEqualTo("tx-123");
        assertThat(model.getSenderUserId()).isEqualTo("alice");
        assertThat(model.getReceiverUserId()).isEqualTo("bob");
        assertThat(model.getAmount()).isEqualTo(42.5);
        assertThat(model.getTransactionDate()).isEqualTo(now);
        assertThat(model.getStatus()).isEqualTo("COMPLETED");
    }

    @Test
    void builder_allowsNulls_whenNotProvided() {
        TransactionRecordModel model = TransactionRecordModel.builder().build();

        assertThat(model).isNotNull();
        assertThat(model.getId()).isNull();
        assertThat(model.getSenderUserId()).isNull();
        assertThat(model.getReceiverUserId()).isNull();
        assertThat(model.getAmount()).isNull();
        assertThat(model.getTransactionDate()).isNull();
        assertThat(model.getStatus()).isNull();
    }
}
