package com.project.user.domain.usecase;

import com.project.common.usecase.UseCaseHandler;
import com.project.user.domain.model.UserWalletSummaryModel;
import com.project.user.domain.port.ReportPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GetUserWalletSummaryHandler implements UseCaseHandler<List<UserWalletSummaryModel>, Void> {

    private static final Logger logger = LoggerFactory.getLogger(GetUserWalletSummaryHandler.class);
    private final ReportPort reportPort;

    public GetUserWalletSummaryHandler(ReportPort reportPort) {
        this.reportPort = reportPort;
    }

    // Hatanın çözümü bu satırda: Parametre olarak (Void input) almak zorunda!
    @Override
    public List<UserWalletSummaryModel> handle(Void input) {
        logger.info("Admin paneli icin Kullanici-Cuzdan ozeti raporu hazirlaniyor.");
        return reportPort.getUserWalletSummaries();
    }
}