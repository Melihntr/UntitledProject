package com.project.transaction.infrastructure.adapter;

import com.project.transaction.domain.model.TransactionRecordModel;
import com.project.transaction.domain.model.WalletModel;
import com.project.transaction.domain.port.TransactionPort;
import com.project.transaction.infrastructure.entity.TransactionRecordEntity;
import com.project.transaction.infrastructure.entity.WalletEntity;
import com.project.transaction.infrastructure.mapper.TransactionInfrastructureMapper;
import com.project.transaction.infrastructure.repository.TransactionRecordRepository;
import com.project.transaction.infrastructure.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Infrastructure persistence adapter implementing the outbound {@link TransactionPort}.
 * This class serves as a secondary adapter in the Hexagonal Architecture (Ports and Adapters) pattern.
 * It bridges the Core Domain and the underlying Spring Data JPA repositories, ensuring that the 
 * domain layer remains completely oblivious to database entities, ORM frameworks, and SQL queries.
 */
@Component
@RequiredArgsConstructor
public class TransactionPersistenceAdapter implements TransactionPort {

    private final WalletRepository walletRepository;
    private final TransactionRecordRepository recordRepository;
    private final TransactionInfrastructureMapper mapper;

    /**
     * Retrieves the wallet associated with the given user ID.
     *
     * @param userId The unique identifier of the user.
     * @return The mapped domain model of the wallet.
     * @throws IllegalArgumentException if no wallet exists for the specified user.
     */
    @Override
    public WalletModel getWalletByUserId(String userId) {
        WalletEntity entity = walletRepository.findByUserId(userId)
                // Note: In an enterprise environment, this is an excellent place to throw a custom 
                // BusinessException (e.g., WalletNotFoundException) that maps to a 404 HTTP status.
                .orElseThrow(() -> new IllegalArgumentException("Wallet not found for user: " + userId));
        
        return mapper.toWalletModel(entity);
    }

    /**
     * Persists the updated state of a wallet to the database.
     * The underlying JPA provider handles the @Version optimistic locking check during the save operation.
     *
     * @param walletModel The updated wallet domain model.
     * @return The saved wallet domain model, reflecting any database-generated updates (like version bumps).
     */
    @Override
    public WalletModel updateWallet(WalletModel walletModel) {
        WalletEntity entity = mapper.toWalletEntity(walletModel);
        WalletEntity savedEntity = walletRepository.save(entity);
        return mapper.toWalletModel(savedEntity);
    }

    /**
     * Persists a newly created transaction record to the database history.
     *
     * @param recordModel The finalized transaction domain model.
     * @return The persisted transaction model.
     */
    @Override
    public TransactionRecordModel saveTransactionRecord(TransactionRecordModel recordModel) {
        TransactionRecordEntity entity = mapper.toRecordEntity(recordModel);
        TransactionRecordEntity savedEntity = recordRepository.save(entity);
        return mapper.toRecordModel(savedEntity);
    }

    /**
     * Queries the database for a paginated list of transaction records belonging to a user,
     * strictly filtered by a specific date range.
     *
     * @param userId    The target user ID.
     * @param startDate The beginning of the date range.
     * @param endDate   The end of the date range.
     * @param pageable  Spring Data pagination and sorting constraints.
     * @return A Page containing mapped transaction domain models.
     */
    @Override
    public Page<TransactionRecordModel> getTransactionHistory(
            String userId, LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {
        
        // Fetch entities from the repository and map them sequentially to domain models
        return recordRepository.findUserTransactionsWithDateFilter(userId, startDate, endDate, pageable)
                .map(mapper::toRecordModel);
    }

    /**
     * Executes a complex database query to identify potentially fraudulent transfer activities.
     *
     * @return A list of raw object arrays containing the aggregated suspicious transaction data.
     */
    @Override
    public List<Object[]> getSuspiciousTransfers() {
        return recordRepository.findSuspiciousTransfersWithSelfJoin();
    }
}
