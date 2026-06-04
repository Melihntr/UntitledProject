package com.project.transaction.domain.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class WalletModelTest {

    @Test
    void builder_and_getters_work() {
        WalletModel wallet = WalletModel.builder()
                .id("w-1")
                .userId("user-1")
                .balance(100.0)
                .version(2L)
                .build();

        assertThat(wallet).isNotNull();
        assertThat(wallet.getId()).isEqualTo("w-1");
        assertThat(wallet.getUserId()).isEqualTo("user-1");
        assertThat(wallet.getBalance()).isEqualTo(100.0);
        assertThat(wallet.getVersion()).isEqualTo(2L);
    }

    @Test
    void addBalance_increasesBalance() {
        WalletModel wallet = WalletModel.builder()
                .id("w-2")
                .userId("user-2")
                .balance(10.0)
                .build();

        wallet.addBalance(15.5);

        assertThat(wallet.getBalance()).isEqualTo(25.5);
    }

    @Test
    void deductBalance_decreasesBalance_whenSufficientFunds() {
        WalletModel wallet = WalletModel.builder()
                .id("w-3")
                .userId("user-3")
                .balance(50.0)
                .build();

        wallet.deductBalance(20.0);

        assertThat(wallet.getBalance()).isEqualTo(30.0);
    }

    @Test
    void deductBalance_allowsExactBalance_toReachZero() {
        WalletModel wallet = WalletModel.builder()
                .id("w-4")
                .userId("user-4")
                .balance(25.0)
                .build();

        wallet.deductBalance(25.0);

        assertThat(wallet.getBalance()).isEqualTo(0.0);
    }

    @Test
    void deductBalance_throwsWhenInsufficientBalance() {
        WalletModel wallet = WalletModel.builder()
                .id("w-5")
                .userId("user-5")
                .balance(5.0)
                .build();

        assertThatThrownBy(() -> wallet.deductBalance(10.0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Insufficient balance");
    }
}