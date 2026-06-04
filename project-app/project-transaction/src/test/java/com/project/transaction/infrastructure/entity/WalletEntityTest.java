package com.project.transaction.infrastructure.entity;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class WalletEntityTest {

    @Test
    void allArgsConstructor_and_getters_work() {
        WalletEntity entity = new WalletEntity(
                "w-123",
                "user-123",
                150.75,
                3L
        );

        assertThat(entity).isNotNull();
        assertThat(entity.getId()).isEqualTo("w-123");
        assertThat(entity.getUserId()).isEqualTo("user-123");
        assertThat(entity.getBalance()).isEqualTo(150.75);
        assertThat(entity.getVersion()).isEqualTo(3L);
    }

    @Test
    void noArgsConstructor_and_setters_work() {
        WalletEntity entity = new WalletEntity();
        entity.setId("w-999");
        entity.setUserId("user-999");
        entity.setBalance(10.0);
        entity.setVersion(1L);

        assertThat(entity.getId()).isEqualTo("w-999");
        assertThat(entity.getUserId()).isEqualTo("user-999");
        assertThat(entity.getBalance()).isEqualTo(10.0);
        assertThat(entity.getVersion()).isEqualTo(1L);
    }
}
