package com.project.transaction.infrastructure.repository;

import com.project.transaction.infrastructure.entity.TransactionRecordEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Spring Data JPA repository for managing {@link TransactionRecordEntity} persistence.
 * This infrastructure component handles both standard CRUD operations and complex 
 * analytical queries (utilizing various SQL JOIN strategies) for reporting and fraud detection.
 * * Enterprise Note: Queries joining multiple entities (like UserEntity and WalletEntity) 
 * assume these tables reside in the same physical database and JPA persistence context.
 */
@Repository
public interface TransactionRecordRepository extends JpaRepository<TransactionRecordEntity, String> {

    /**
     * Standard Query: Retrieves a paginated transaction history for a specific user.
     * Uses OR logic to fetch records where the user acted as either the sender or receiver,
     * restricted by a strict date range filter.
     *
     * @param userId    The ID of the user (sender or receiver).
     * @param startDate The start of the date range.
     * @param endDate   The end of the date range.
     * @param pageable  Pagination and sorting parameters.
     * @return A paginated list of transaction records.
     */
    @Query("SELECT t FROM TransactionRecordEntity t " +
           "WHERE (t.senderUserId = :userId OR t.receiverUserId = :userId) " +
           "AND t.transactionDate >= :startDate " +
           "AND t.transactionDate <= :endDate")
    Page<TransactionRecordEntity> findUserTransactionsWithDateFilter(
            @Param("userId") String userId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable);

    /**
     * 1. INNER JOIN (Strict Intersection)
     * Retrieves only records where a match exists in both User and Wallet tables.
     * * Business Scenario: Fetching VIP users whose wallet balance exceeds a specific threshold.
     *
     * @param minBalance The minimum balance required to be considered a VIP.
     * @return A list of arrays containing username and balance.
     */
    @Query("SELECT u.username, w.balance FROM UserEntity u " +
           "INNER JOIN WalletEntity w ON u.id = w.userId " +
           "WHERE w.balance > :minBalance")
    List<Object[]> findVipUsersWithInnerJoin(@Param("minBalance") Double minBalance);

    /**
     * 2. LEFT JOIN (All records from the Left table)
     * Retrieves all users. If a user does not have an associated wallet, the balance returns as NULL.
     * * Business Scenario: Generating a comprehensive user report to identify users without wallets 
     * in order to send them "Create a Wallet" marketing notifications.
     *
     * @return A list of arrays containing username and balance (potentially null).
     */
    @Query("SELECT u.username, w.balance FROM UserEntity u " +
           "LEFT JOIN WalletEntity w ON u.id = w.userId")
    List<Object[]> findAllUsersAndBalancesLeftJoin();

    /**
     * 3. RIGHT JOIN (All records from the Right table)
     * Retrieves all transaction records. If the associated sender user has been hard-deleted 
     * from the system, the transaction record is still retrieved with a NULL username.
     * * Business Scenario: Ensuring financial audit logs remain intact and historical transfers 
     * are listed even if the initiating user account no longer exists.
     *
     * @return A list of arrays containing transaction details and potentially null usernames.
     */
    @Query("SELECT t.id, t.amount, u.username FROM UserEntity u " +
           "RIGHT JOIN TransactionRecordEntity t ON u.id = t.senderUserId")
    List<Object[]> findAllTransactionsEvenIfUserDeletedRightJoin();

    /**
     * 4. SELF JOIN (Joining a table to itself)
     * * Business Scenario: Fraud Detection (Anti-Money Laundering - AML).
     * Compares transaction records against other records within the same table to identify 
     * suspicious activity, such as a single sender making multiple high-value transfers.
     *
     * @return A list of arrays containing pairs of suspicious high-value transactions from the same sender.
     */
    @Query("SELECT t1.id, t1.amount, t2.id, t2.amount FROM TransactionRecordEntity t1 " +
           "INNER JOIN TransactionRecordEntity t2 ON t1.senderUserId = t2.senderUserId " +
           "WHERE t1.id <> t2.id AND t1.amount > 5000 AND t2.amount > 5000")
    List<Object[]> findSuspiciousTransfersWithSelfJoin();

}