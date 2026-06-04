package com.project.user.infrastructure.adapter;

import com.project.user.domain.model.ActiveTransferUserModel;
import com.project.user.domain.model.OrphanWalletModel;
import com.project.user.domain.model.UserWalletSummaryModel;
import com.project.user.domain.port.ReportPort;
import com.project.user.infrastructure.repository.AdminReportRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * ReportPort arayuzunun (Outbound Port) implementasyonudur.
 * Spring Boot'un "Bean could not be found" hatasini cozen ana bilesendir.
 */
@Component
public class ReportPersistenceAdapter implements ReportPort {

    private final AdminReportRepository reportRepository;

    public ReportPersistenceAdapter(AdminReportRepository reportRepository) {
        this.reportRepository = reportRepository;
    }

    @Override
    public List<UserWalletSummaryModel> getUserWalletSummaries() {
        return reportRepository.findUserWalletSummaries().stream()
                // ÇÖZÜM BURADA: projection tipini açıkça belirttik
                .map((AdminReportRepository.UserWalletSummary projection) -> UserWalletSummaryModel.builder()
                        .username(projection.getUsername())
                        .email(projection.getEmail())
                        .balance(projection.getBalance())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public List<ActiveTransferUserModel> getActiveTransferUsers() {
        return reportRepository.findActiveTransferUsers().stream()
                // ÇÖZÜM BURADA
                .map((AdminReportRepository.ActiveTransferUser projection) -> ActiveTransferUserModel.builder()
                        .username(projection.getUsername())
                        .amount(projection.getAmount())
                        .createdAt(projection.getCreatedAt())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public List<OrphanWalletModel> getOrphanWallets() {
        return reportRepository.findOrphanWallets().stream()
                // ÇÖZÜM BURADA
                .map((AdminReportRepository.OrphanWallet projection) -> OrphanWalletModel.builder()
                        .walletId(projection.getWalletId())
                        .balance(projection.getBalance())
                        .supposedUserId(projection.getSupposedUserId())
                        .build())
                .collect(Collectors.toList());
    }
}