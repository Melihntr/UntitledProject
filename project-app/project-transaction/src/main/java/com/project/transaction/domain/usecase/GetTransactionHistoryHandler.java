package com.project.transaction.domain.usecase;

import com.project.common.usecase.UseCaseHandler;
import com.project.transaction.domain.model.TransactionRecordModel;
import com.project.transaction.domain.port.TransactionPort;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * Core business use case handler for retrieving a user's transaction history.
 * This class encapsulates the business rules and validations required before 
 * querying the infrastructure layer for historical financial records.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class GetTransactionHistoryHandler implements UseCaseHandler<Page<TransactionRecordModel>, GetTransactionHistoryHandler.HistoryFilterInput> {

    private final TransactionPort transactionPort;

    /**
     * Domain input model specifically designed to encapsulate the filtering criteria 
     * for a transaction history query.
     */
    @Getter
    @Builder
    public static class HistoryFilterInput {
        private String userId;
        private LocalDateTime startDate;
        private LocalDateTime endDate;
        private Pageable pageable;
    }

    /**
     * Executes the business logic to fetch the paginated transaction history.
     * Enforces domain invariants, such as ensuring the date range is logically valid.
     *
     * @param input The filtering criteria containing user ID, date range, and pagination details.
     * @return A paginated list of transaction records matching the criteria.
     * @throws IllegalArgumentException if the start date is chronologically after the end date.
     */
    @Override
    public Page<TransactionRecordModel> handle(HistoryFilterInput input) {
        
        log.info("Fetching transaction history for user ID: {} from {} to {}",
                input.getUserId(), input.getStartDate(), input.getEndDate());

        // Domain Validation: The start date cannot be in the future relative to the end date.
        // Future Extension Point: Additional business rules can be injected here, such as 
        // restricting the maximum date range (e.g., max 30 days per query) to prevent database overload.
        if (input.getStartDate().isAfter(input.getEndDate())) {
            log.warn("Invalid date range provided by user {}: StartDate={}, EndDate={}",
                    input.getUserId(), input.getStartDate(), input.getEndDate());
            // Note: In a production setup, throwing your custom BusinessException is highly recommended here.
            throw new IllegalArgumentException("Business Rule Violation: Start date cannot be after the end date.");
        }

        // Delegate the validated data retrieval request to the infrastructure port
        return transactionPort.getTransactionHistory(
                input.getUserId(), 
                input.getStartDate(), 
                input.getEndDate(), 
                input.getPageable()
        );
    }
}
