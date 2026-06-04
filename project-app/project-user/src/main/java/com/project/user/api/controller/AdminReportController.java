package com.project.user.api.controller;

import com.project.common.model.GenericResponse;
import com.project.user.api.dto.ActiveTransferUserResponse;
import com.project.user.api.dto.OrphanWalletResponse;
import com.project.user.api.dto.UserWalletSummaryResponse;
import com.project.user.api.mapper.AdminReportApiMapper;
import com.project.user.domain.model.UserWalletSummaryModel;
import com.project.user.domain.usecase.GetActiveTransferUsersHandler;
import com.project.user.domain.usecase.GetOrphanWalletsHandler;
import com.project.user.domain.usecase.GetUserWalletSummaryHandler;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.project.user.domain.model.ActiveTransferUserModel;
import com.project.user.domain.model.OrphanWalletModel;
import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/reports")
public class AdminReportController {

    private final GetUserWalletSummaryHandler userWalletSummaryHandler;
    private final GetActiveTransferUsersHandler activeTransferUsersHandler;
    private final GetOrphanWalletsHandler orphanWalletsHandler;
    private final AdminReportApiMapper mapper;

    public AdminReportController(GetUserWalletSummaryHandler userWalletSummaryHandler,
                                 GetActiveTransferUsersHandler activeTransferUsersHandler,
                                 GetOrphanWalletsHandler orphanWalletsHandler,
                                 AdminReportApiMapper mapper) {
        this.userWalletSummaryHandler = userWalletSummaryHandler;
        this.activeTransferUsersHandler = activeTransferUsersHandler;
        this.orphanWalletsHandler = orphanWalletsHandler;
        this.mapper = mapper;
    }

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