package com.project.user.infrastructure.api.controller;

import com.project.common.model.GenericResponse;
import com.project.user.infrastructure.api.dto.ActiveTransferUserResponse;
import com.project.user.infrastructure.api.dto.OrphanWalletResponse;
import com.project.user.infrastructure.api.dto.UserWalletSummaryResponse;
import com.project.user.infrastructure.api.mapper.AdminReportApiMapper;
import com.project.user.domain.model.ActiveTransferUserModel;
import com.project.user.domain.model.OrphanWalletModel;
import com.project.user.domain.model.UserWalletSummaryModel;
import com.project.user.domain.handler.GetActiveTransferUsersHandler;
import com.project.user.domain.handler.GetOrphanWalletsHandler;
import com.project.user.domain.handler.GetUserWalletSummaryHandler;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminReportControllerTest {

    @Mock
    private GetUserWalletSummaryHandler userWalletSummaryHandler;

    @Mock
    private GetActiveTransferUsersHandler activeTransferUsersHandler;

    @Mock
    private GetOrphanWalletsHandler orphanWalletsHandler;

    @Mock
    private AdminReportApiMapper mapper;

    @InjectMocks
    private AdminReportController controller;

    @Test
    void getUserWalletSummary_returnsMappedResponses() {
        List<UserWalletSummaryModel> models = List.of(UserWalletSummaryModel.builder().username("alice").build());
        List<UserWalletSummaryResponse> responses = List.of(UserWalletSummaryResponse.builder().username("alice").build());
        when(userWalletSummaryHandler.handle(null)).thenReturn(models);
        when(mapper.toSummaryResponseList(models)).thenReturn(responses);

        ResponseEntity<GenericResponse<List<UserWalletSummaryResponse>>> result = controller.getUserWalletSummary();

        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().getData()).isSameAs(responses);
    }

    @Test
    void getActiveTransferUsers_returnsMappedResponses() {
        List<ActiveTransferUserModel> models = List.of(ActiveTransferUserModel.builder().username("alice").build());
        List<ActiveTransferUserResponse> responses = List.of(ActiveTransferUserResponse.builder().username("alice").build());
        when(activeTransferUsersHandler.handle(null)).thenReturn(models);
        when(mapper.toActiveUserResponseList(models)).thenReturn(responses);

        ResponseEntity<GenericResponse<List<ActiveTransferUserResponse>>> result = controller.getActiveTransferUsers();

        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().getData()).isSameAs(responses);
    }

    @Test
    void getOrphanWallets_returnsMappedResponses() {
        List<OrphanWalletModel> models = List.of(OrphanWalletModel.builder().walletId("w1").build());
        List<OrphanWalletResponse> responses = List.of(OrphanWalletResponse.builder().walletId("w1").build());
        when(orphanWalletsHandler.handle(null)).thenReturn(models);
        when(mapper.toOrphanWalletResponseList(models)).thenReturn(responses);

        ResponseEntity<GenericResponse<List<OrphanWalletResponse>>> result = controller.getOrphanWallets();

        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().getData()).isSameAs(responses);
    }
}
