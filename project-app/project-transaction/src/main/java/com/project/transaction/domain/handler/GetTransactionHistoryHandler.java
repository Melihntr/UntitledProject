package com.project.transaction.domain.handler;

import com.project.common.domain.usecase.UseCaseHandler;
import com.project.transaction.domain.model.TransactionRecordModel;
import com.project.transaction.domain.port.TransactionPort;
import com.project.transaction.domain.usecase.HistoryFilterInput;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

/**
 * Core business use case handler for retrieving a user's transaction history.
 * This class encapsulates the business rules and validations required before 
 * querying the infrastructure layer for historical financial records.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class GetTransactionHistoryHandler implements UseCaseHandler<Page<TransactionRecordModel>, HistoryFilterInput> {

    private final TransactionPort transactionPort;

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
        
        log.info("transaction.history.request userId={} startDate={} endDate={} page={} size={}",
                input.getUserId(), input.getStartDate(), input.getEndDate(),
                input.getPageable().getPageNumber(), input.getPageable().getPageSize());

        // Domain Validation: The start date cannot be in the future relative to the end date.
        // Future Extension Point: Additional business rules can be injected here, such as 
        // restricting the maximum date range (e.g., max 30 days per query) to prevent database overload.
        if (input.getStartDate().isAfter(input.getEndDate())) {
            log.warn("transaction.history.rejected userId={} startDate={} endDate={} reason=INVALID_DATE_RANGE",
                    input.getUserId(), input.getStartDate(), input.getEndDate());
            // Note: In a production setup, throwing your custom BusinessException is highly recommended here.
            throw new IllegalArgumentException("Business Rule Violation: Start date cannot be after the end date.");
        }

        // Delegate the validated data retrieval request to the infrastructure port
        Page<TransactionRecordModel> result = transactionPort.getTransactionHistory(
                input.getUserId(), 
                input.getStartDate(), 
                input.getEndDate(), 
                input.getPageable()
        );
        log.info("transaction.history.success userId={} resultCount={} totalElements={}",
                input.getUserId(), result.getNumberOfElements(), result.getTotalElements());
        return result;
    }
}
