package com.project.transaction.domain.usecase;

import com.project.common.usecase.UseCaseHandler;
import com.project.transaction.domain.model.TransactionInput;
import com.project.transaction.domain.model.TransactionRecordModel;
import com.project.transaction.domain.model.WalletModel;
import com.project.transaction.domain.port.TransactionPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * Core business use case handler for executing a financial balance transfer between two wallets.
 * This class orchestrates the primary domain logic, ensuring ACID compliance across database 
 * operations via a single @Transactional boundary, followed by a synchronous inter-service 
 * communication call to the Notification Microservice.
 */
@Service
public class ExecuteTransferHandler implements UseCaseHandler<TransactionRecordModel, TransactionInput> {

    private static final Logger logger = LoggerFactory.getLogger(ExecuteTransferHandler.class);

    private final TransactionPort transactionPort;
    private final RestTemplate restTemplate;

    // Dependency Injection via constructor
    public ExecuteTransferHandler(TransactionPort transactionPort, RestTemplate restTemplate) {
        this.transactionPort = transactionPort;
        this.restTemplate = restTemplate;
    }

    /**
     * Executes the money transfer process.
     * The @Transactional annotation guarantees atomicity. If any database operation fails 
     * (e.g., wallet not found, insufficient balance, or OptimisticLockException due to concurrent updates),
     * the entire transaction is automatically rolled back, preventing data inconsistency.
     *
     * @param input The validated domain input containing sender, receiver, and amount details.
     * @return The finalized transaction record.
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public TransactionRecordModel handle(TransactionInput input) {
        
        logger.info("Initiating transfer of {} from User {} to User {}", 
                input.getAmount(), input.getSenderUserId(), input.getReceiverUserId());

        // Step 1: Fetch and update the Sender's Wallet
        WalletModel senderWallet = transactionPort.getWalletByUserId(input.getSenderUserId());
        senderWallet.deductBalance(input.getAmount());
        transactionPort.updateWallet(senderWallet); // Infrastructure adapter will enforce @Version checks

        // Step 2: Fetch and update the Receiver's Wallet
        WalletModel receiverWallet = transactionPort.getWalletByUserId(input.getReceiverUserId());
        receiverWallet.addBalance(input.getAmount());
        transactionPort.updateWallet(receiverWallet); // Infrastructure adapter will enforce @Version checks

        // Step 3: Create and persist the Transaction History record
        TransactionRecordModel record = TransactionRecordModel.builder()
                .id(UUID.randomUUID().toString())
                .senderUserId(input.getSenderUserId())
                .receiverUserId(input.getReceiverUserId())
                .amount(input.getAmount())
                .transactionDate(LocalDateTime.now())
                .status("COMPLETED")
                .build();
                
        TransactionRecordModel savedRecord = transactionPort.saveTransactionRecord(record);

        // Step 4: Inter-Service Communication (Synchronous REST Call)
        // Send an alert to the independent Notification Microservice running on port 8081.
        try {
            String notificationServiceUrl = "http://localhost:8081/api/v1/notifications/send";
            
            // Construct the payload for the Notification Service
            Map<String, String> payload = Map.of(
                    "recipientId", savedRecord.getReceiverUserId(),
                    "message", "You received a transfer of " + savedRecord.getAmount() + " TL from user: " + savedRecord.getSenderUserId()
            );

            // Execute the HTTP POST request
            restTemplate.postForEntity(notificationServiceUrl, payload, String.class);
            
            logger.info("Notification successfully dispatched to the Notification Microservice.");
        } catch (Exception e) {
            // Enterprise Note: In a highly resilient distributed system, synchronous HTTP calls 
            // within a core transaction can be risky. We catch the exception here so that a 
            // notification failure does not roll back a successful financial transaction.
            // Ideally, this would use the Transactional Outbox Pattern with an event broker (e.g., Kafka).
            logger.error("Failed to reach the Notification Service. Reason: {}", e.getMessage());
        }

        return savedRecord;
    }
}