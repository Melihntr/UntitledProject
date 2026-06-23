package com.project.transaction.infrastructure.entity;

import com.project.user.infrastructure.entity.UserEntity;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class TransactionRecordEntityTest {

    @Test
    void settersAndGettersIncludeBaseId() {
        LocalDateTime now = LocalDateTime.now();

        TransactionRecordEntity entity = new TransactionRecordEntity();
        entity.setId("tx-123");
        entity.setSenderUserId("alice");
        entity.setReceiverUserId("bob");
        entity.setAmount(42.5);
        entity.setTransactionDate(now);
        entity.setStatus("COMPLETED");

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
        UserEntity sender = new UserEntity();
        UserEntity receiver = new UserEntity();
        entity.setSender(sender);
        entity.setReceiver(receiver);
        entity.setAmount(10.0);
        entity.setTransactionDate(then);
        entity.setStatus("PENDING");

        assertThat(entity.getId()).isEqualTo("tx-999");
        assertThat(entity.getSenderUserId()).isEqualTo("carol");
        assertThat(entity.getReceiverUserId()).isEqualTo("dave");
        assertThat(entity.getSender()).isSameAs(sender);
        assertThat(entity.getReceiver()).isSameAs(receiver);
        assertThat(entity.getAmount()).isEqualTo(10.0);
        assertThat(entity.getTransactionDate()).isEqualTo(then);
        assertThat(entity.getStatus()).isEqualTo("PENDING");
    }

    @Test
    void noArgsConstructor_leavesIdForPersistenceGeneration() {
        TransactionRecordEntity entity = new TransactionRecordEntity();

        assertThat(entity.getId()).isNull();
    }
}
