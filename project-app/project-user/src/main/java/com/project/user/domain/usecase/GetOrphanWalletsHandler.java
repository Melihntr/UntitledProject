package com.project.user.domain.usecase;

import com.project.common.usecase.UseCaseHandler;
import com.project.user.domain.model.OrphanWalletModel;
import com.project.user.domain.port.ReportPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class GetOrphanWalletsHandler implements UseCaseHandler<List<OrphanWalletModel>, Void> {

    private final ReportPort reportPort;

    @Override
    public List<OrphanWalletModel> handle(Void input) {
        log.info("report.orphan-wallets.request");
        List<OrphanWalletModel> result = reportPort.getOrphanWallets();
        log.info("report.orphan-wallets.success resultCount={}", result.size());
        return result;
    }
}
