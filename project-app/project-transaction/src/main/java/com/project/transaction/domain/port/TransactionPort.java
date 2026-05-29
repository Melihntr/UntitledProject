package com.project.transaction.domain.port;

import com.project.transaction.domain.model.TransactionRecordModel;
import com.project.transaction.domain.model.WalletModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Outbound (Driven) Port interface for the Transaction domain.
 * This contract defines the data operations required by the core business logic,
 * ensuring that the domain layer remains fully decoupled from specific database
 * technologies, ORMs, or Spring Data JPA implementations.
 */
public interface TransactionPort {
    
    /**
     * Retrieves a user's wallet based on their unique user ID.
     *
     * @param userId The unique identifier of the wallet owner.
     * @return The domain model representing the current state of the wallet.
     */
    WalletModel getWalletByUserId(String userId);
    
    /**
     * Persists the updated state of a wallet (e.g., after a balance deduction or addition).
     *
     * @param walletModel The updated wallet domain model.
     * @return The saved wallet domain model, including the incremented optimistic locking version.
     */
    WalletModel updateWallet(WalletModel walletModel);
    
    /**
     * Records a completed or attempted financial transaction into the persistence store.
     *
     * @param recordModel The finalized transaction domain model.
     * @return The persisted transaction record, containing its newly generated database ID.
     */
    TransactionRecordModel saveTransactionRecord(TransactionRecordModel recordModel);
    
    /**
     * Retrieves a paginated history of transactions involving a specific user within a given timeframe.
     *
     * @param userId    The unique identifier of the user whose history is being queried.
     * @param startDate The start date for the history filter.
     * @param endDate   The end date for the history filter.
     * @param pageable  Pagination and sorting parameters provided by the client.
     * @return A paginated list of transaction records.
     */
    Page<TransactionRecordModel> getTransactionHistory(
            String userId, LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

    /**
     * Retrieves a system-wide report of potentially fraudulent or suspicious transfer records.
     * This administrative function typically relies on complex aggregations in the infrastructure layer.
     *
     * @return A list of raw object arrays representing aggregated suspicious transaction data.
     */
    List<Object[]> getSuspiciousTransfers();
}