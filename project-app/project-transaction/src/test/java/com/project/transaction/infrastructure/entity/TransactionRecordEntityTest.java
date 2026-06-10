package com.project.transaction.infrastructure.entity;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class TransactionRecordEntityTest {

    @Test
    void allArgsConstructor_and_getters_work() {
        LocalDateTime now = LocalDateTime.now();

        TransactionRecordEntity entity = new TransactionRecordEntity(
                "tx-123",
                "alice",
                "bob",
                42.5,
                now,
                "COMPLETED"
        );

        assertThat(entity).isNotNull();
        assertThat(entity.getId()).isEqualTo("tx-123");
        assertThat(entity.getSenderUserId()).isEqualTo("alice");
        assertThat(entity.getReceiverUserId()).isEqualTo("bob");
        assertThat(entity.getAmount()).isEqualTo(42.5);
        assertThat(entity.getTransactionDate()).isEqualTo(now);
        assertThat(entity.getStatus()).isEqualTo("COMPLETED");
    }

    @Test
    void noArgsConstructor_and_setters_work() {
        LocalDateTime then = LocalDateTime.of(2025, 1, 1, 12, 0);

        TransactionRecordEntity entity = new TransactionRecordEntity();
        entity.setId("tx-999");
        entity.setSenderUserId("carol");
        entity.setReceiverUserId("dave");
        entity.setAmount(10.0);
        entity.setTransactionDate(then);
        entity.setStatus("PENDING");

        assertThat(entity.getId()).isEqualTo("tx-999");
        assertThat(entity.getSenderUserId()).isEqualTo("carol");
        assertThat(entity.getReceiverUserId()).isEqualTo("dave");
        assertThat(entity.getAmount()).isEqualTo(10.0);
        assertThat(entity.getTransactionDate()).isEqualTo(then);
        assertThat(entity.getStatus()).isEqualTo("PENDING");
    }

    @Test
    void noArgsConstructor_generatesId() {
        TransactionRecordEntity entity = new TransactionRecordEntity();

        assertThat(entity.getId()).isNotBlank();
    }

    @Test
    void assignIdIfMissing_generatesIdWhenIdIsBlank() {
        TransactionRecordEntity entity = new TransactionRecordEntity();
        entity.setId(" ");

        entity.assignIdIfMissing();

        assertThat(entity.getId()).isNotBlank();
    }

    @Test
    void assignIdIfMissing_generatesIdWhenIdIsNull() {
        TransactionRecordEntity entity = new TransactionRecordEntity();
        entity.setId(null);

        entity.assignIdIfMissing();

        assertThat(entity.getId()).isNotBlank();
    }

    @Test
    void assignIdIfMissing_keepsExistingId() {
        TransactionRecordEntity entity = new TransactionRecordEntity();
        entity.setId("transaction-1");

        entity.assignIdIfMissing();

        assertThat(entity.getId()).isEqualTo("transaction-1");
    }
}
