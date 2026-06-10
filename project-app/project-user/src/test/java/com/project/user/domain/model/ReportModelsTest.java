package com.project.user.domain.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class ReportModelsTest {

    @Test
    void activeTransferUserModelBuilderWorks() {
        LocalDateTime createdAt = LocalDateTime.of(2026, 1, 1, 10, 0);

        ActiveTransferUserModel model = ActiveTransferUserModel.builder()
                .username("alice")
                .amount(BigDecimal.TEN)
                .createdAt(createdAt)
                .build();

        assertThat(model.getUsername()).isEqualTo("alice");
        assertThat(model.getAmount()).isEqualByComparingTo(BigDecimal.TEN);
        assertThat(model.getCreatedAt()).isEqualTo(createdAt);
    }

    @Test
    void orphanWalletModelConstructorsBuilderAndSettersWork() {
        OrphanWalletModel model = OrphanWalletModel.builder()
                .walletId("w1")
                .balance(BigDecimal.ONE)
                .supposedUserId("u1")
                .build();
        OrphanWalletModel allArgs = new OrphanWalletModel("w2", BigDecimal.TEN, "u2");
        OrphanWalletModel noArgs = new OrphanWalletModel();
        noArgs.setWalletId("w3");

        assertThat(model.getWalletId()).isEqualTo("w1");
        assertThat(allArgs.getSupposedUserId()).isEqualTo("u2");
        assertThat(noArgs.getWalletId()).isEqualTo("w3");
    }

    @Test
    void userWalletSummaryModelSafeBalanceDefaultsNullToZero() {
        UserWalletSummaryModel withBalance = UserWalletSummaryModel.builder()
                .username("alice")
                .email("alice@example.com")
                .balance(BigDecimal.TEN)
                .build();
        UserWalletSummaryModel withoutBalance = UserWalletSummaryModel.builder().build();

        assertThat(withBalance.getSafeBalance()).isEqualByComparingTo(BigDecimal.TEN);
        assertThat(withoutBalance.getSafeBalance()).isEqualByComparingTo(BigDecimal.ZERO);
    }
}
