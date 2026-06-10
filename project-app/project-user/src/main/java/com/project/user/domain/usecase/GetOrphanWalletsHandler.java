package com.project.user.domain.usecase;

import com.project.common.usecase.UseCaseHandler;
import com.project.user.domain.model.OrphanWalletModel;
import com.project.user.domain.port.ReportPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GetOrphanWalletsHandler implements UseCaseHandler<List<OrphanWalletModel>, Void> {

    private final ReportPort reportPort;

    @Override
    public List<OrphanWalletModel> handle(Void input) {
        return reportPort.getOrphanWallets();
    }
}
