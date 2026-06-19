package com.project.user.domain.handler;

import com.project.common.usecase.UseCaseHandler;
import com.project.user.domain.model.UserWalletSummaryModel;
import com.project.user.domain.port.ReportPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class GetUserWalletSummaryHandler implements UseCaseHandler<List<UserWalletSummaryModel>, Void> {

    private final ReportPort reportPort;

    // Hatanın çözümü bu satırda: Parametre olarak (Void input) almak zorunda!
    @Override
    public List<UserWalletSummaryModel> handle(Void input) {
        log.info("report.user-wallet-summary.request");
        List<UserWalletSummaryModel> result = reportPort.getUserWalletSummaries();
        log.info("report.user-wallet-summary.success resultCount={}", result.size());
        return result;
    }
}
