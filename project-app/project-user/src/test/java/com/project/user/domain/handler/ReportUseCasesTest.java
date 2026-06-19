package com.project.user.domain.handler;

import com.project.user.domain.model.ActiveTransferUserModel;
import com.project.user.domain.model.OrphanWalletModel;
import com.project.user.domain.model.UserWalletSummaryModel;
import com.project.user.domain.port.ReportPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReportUseCasesTest {

    @Mock
    private ReportPort reportPort;

    @Test
    void getActiveTransferUsers_delegatesToReportPort() {
        List<ActiveTransferUserModel> expected = List.of(ActiveTransferUserModel.builder().username("alice").build());
        when(reportPort.getActiveTransferUsers()).thenReturn(expected);

        List<ActiveTransferUserModel> result = new GetActiveTransferUsersHandler(reportPort).handle(null);

        assertThat(result).isSameAs(expected);
        verify(reportPort).getActiveTransferUsers();
    }

    @Test
    void getOrphanWallets_delegatesToReportPort() {
        List<OrphanWalletModel> expected = List.of(OrphanWalletModel.builder().walletId("w1").build());
        when(reportPort.getOrphanWallets()).thenReturn(expected);

        List<OrphanWalletModel> result = new GetOrphanWalletsHandler(reportPort).handle(null);

        assertThat(result).isSameAs(expected);
        verify(reportPort).getOrphanWallets();
    }

    @Test
    void getUserWalletSummary_delegatesToReportPort() {
        List<UserWalletSummaryModel> expected = List.of(UserWalletSummaryModel.builder().username("alice").build());
        when(reportPort.getUserWalletSummaries()).thenReturn(expected);

        List<UserWalletSummaryModel> result = new GetUserWalletSummaryHandler(reportPort).handle(null);

        assertThat(result).isSameAs(expected);
        verify(reportPort).getUserWalletSummaries();
    }
}
