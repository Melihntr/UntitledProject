package com.project.user.infrastructure.adapter;

import com.project.user.domain.model.ActiveTransferUserModel;
import com.project.user.domain.model.OrphanWalletModel;
import com.project.user.domain.model.UserWalletSummaryModel;
import com.project.user.infrastructure.repository.AdminReportRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReportPersistenceAdapterTest {

    @Mock
    private AdminReportRepository reportRepository;

    @InjectMocks
    private ReportPersistenceAdapter adapter;

    @Test
    void getUserWalletSummaries_mapsProjection() {
        AdminReportRepository.UserWalletSummary projection = new AdminReportRepository.UserWalletSummary() {
            @Override public String getUsername() { return "alice"; }
            @Override public String getEmail() { return "alice@example.com"; }
            @Override public BigDecimal getBalance() { return BigDecimal.TEN; }
        };
        when(reportRepository.findUserWalletSummaries()).thenReturn(List.of(projection));

        List<UserWalletSummaryModel> result = adapter.getUserWalletSummaries();

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getUsername()).isEqualTo("alice");
        assertThat(result.getFirst().getBalance()).isEqualByComparingTo(BigDecimal.TEN);
    }

    @Test
    void getActiveTransferUsers_mapsProjection() {
        LocalDateTime createdAt = LocalDateTime.of(2026, 1, 1, 10, 0);
        AdminReportRepository.ActiveTransferUser projection = new AdminReportRepository.ActiveTransferUser() {
            @Override public String getUsername() { return "alice"; }
            @Override public BigDecimal getAmount() { return BigDecimal.ONE; }
            @Override public LocalDateTime getCreatedAt() { return createdAt; }
        };
        when(reportRepository.findActiveTransferUsers()).thenReturn(List.of(projection));

        List<ActiveTransferUserModel> result = adapter.getActiveTransferUsers();

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getUsername()).isEqualTo("alice");
        assertThat(result.getFirst().getAmount()).isEqualByComparingTo(BigDecimal.ONE);
        assertThat(result.getFirst().getCreatedAt()).isEqualTo(createdAt);
    }

    @Test
    void getOrphanWallets_mapsProjection() {
        AdminReportRepository.OrphanWallet projection = new AdminReportRepository.OrphanWallet() {
            @Override public String getWalletId() { return "wallet-1"; }
            @Override public BigDecimal getBalance() { return BigDecimal.ZERO; }
            @Override public String getSupposedUserId() { return "missing-user"; }
        };
        when(reportRepository.findOrphanWallets()).thenReturn(List.of(projection));

        List<OrphanWalletModel> result = adapter.getOrphanWallets();

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getWalletId()).isEqualTo("wallet-1");
        assertThat(result.getFirst().getSupposedUserId()).isEqualTo("missing-user");
    }
}
