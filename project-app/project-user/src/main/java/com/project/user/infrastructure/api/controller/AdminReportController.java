package com.project.user.infrastructure.api.controller;

import com.project.common.model.GenericResponse;
import com.project.user.infrastructure.api.dto.ActiveTransferUserResponse;
import com.project.user.infrastructure.api.dto.OrphanWalletResponse;
import com.project.user.infrastructure.api.dto.UserWalletSummaryResponse;
import com.project.user.infrastructure.api.mapper.AdminReportApiMapper;
import com.project.user.domain.model.UserWalletSummaryModel;
import com.project.user.domain.handler.GetActiveTransferUsersHandler;
import com.project.user.domain.handler.GetOrphanWalletsHandler;
import com.project.user.domain.handler.GetUserWalletSummaryHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.project.user.domain.model.ActiveTransferUserModel;
import com.project.user.domain.model.OrphanWalletModel;
import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/reports")
@RequiredArgsConstructor
public class AdminReportController {

    private final GetUserWalletSummaryHandler userWalletSummaryHandler;
    private final GetActiveTransferUsersHandler activeTransferUsersHandler;
    private final GetOrphanWalletsHandler orphanWalletsHandler;
    private final AdminReportApiMapper mapper;

    @GetMapping("/user-wallet-summary")
    public ResponseEntity<GenericResponse<List<UserWalletSummaryResponse>>> getUserWalletSummary() {
        // var yerine net tipleri yazdik ve null degerini (Void) olarak belirttik
        List<UserWalletSummaryModel> models = userWalletSummaryHandler.handle((Void) null);
        List<UserWalletSummaryResponse> responses = mapper.toSummaryResponseList(models);
        return ResponseEntity.ok(GenericResponse.success(responses));
    }

    @GetMapping("/active-transfers")
    public ResponseEntity<GenericResponse<List<ActiveTransferUserResponse>>> getActiveTransferUsers() {
        List<ActiveTransferUserModel> models = activeTransferUsersHandler.handle((Void) null);
        List<ActiveTransferUserResponse> responses = mapper.toActiveUserResponseList(models);
        return ResponseEntity.ok(GenericResponse.success(responses));
    }

    @GetMapping("/health/orphan-wallets")
    public ResponseEntity<GenericResponse<List<OrphanWalletResponse>>> getOrphanWallets() {
        List<OrphanWalletModel> models = orphanWalletsHandler.handle((Void) null);
        List<OrphanWalletResponse> responses = mapper.toOrphanWalletResponseList(models);
        return ResponseEntity.ok(GenericResponse.success(responses));
    }
}
