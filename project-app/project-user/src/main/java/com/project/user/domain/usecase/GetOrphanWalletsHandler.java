package com.project.user.domain.usecase;

import com.project.common.usecase.UseCaseHandler;
import com.project.user.domain.model.OrphanWalletModel;
import com.project.user.domain.port.ReportPort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GetOrphanWalletsHandler implements UseCaseHandler<List<OrphanWalletModel>, Void> {

    private final ReportPort reportPort;

    public GetOrphanWalletsHandler(ReportPort reportPort) {
        this.reportPort = reportPort;
    }

    @Override
    public List<OrphanWalletModel> handle(Void input) {
        return reportPort.getOrphanWallets();
    }
}