package com.project.transaction.domain.usecase;

import com.project.transaction.domain.model.TransactionRecordModel;
import com.project.transaction.domain.port.TransactionPort;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class GetTransactionHistoryHandler {

    private final TransactionPort transactionPort;

    public GetTransactionHistoryHandler(TransactionPort transactionPort) {
        this.transactionPort = transactionPort;
    }

    // Input DTO (Filtreleme kriterleri)
    @Getter
    @Builder
    public static class HistoryFilterInput {
        private String userId;
        private LocalDateTime startDate;
        private LocalDateTime endDate;
        private Pageable pageable;
    }

    public Page<TransactionRecordModel> handle(HistoryFilterInput input) {
        // İstenirse burada ekstra business kuralları (Örn: Maksimum 30 günlük tarih seçilebilir) eklenebilir.
        if (input.getStartDate().isAfter(input.getEndDate())) {
            throw new IllegalArgumentException("Start date cannot be after end date"); // Veya BusinessException
        }
        return transactionPort.getTransactionHistory(
                input.getUserId(), input.getStartDate(), input.getEndDate(), input.getPageable());
    }
}