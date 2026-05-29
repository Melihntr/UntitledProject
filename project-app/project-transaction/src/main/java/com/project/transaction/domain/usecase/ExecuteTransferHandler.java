package com.project.transaction.domain.usecase;

import com.project.common.usecase.UseCaseHandler;
import com.project.transaction.domain.model.TransactionInput;
import com.project.transaction.domain.model.TransactionRecordModel;
import com.project.transaction.domain.model.WalletModel;
import com.project.transaction.domain.port.TransactionPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Core business logic for executing a balance transfer.
 * Demonstrates multiple database operations within a single @Transactional boundary.
 */
@Service
public class ExecuteTransferHandler implements UseCaseHandler<TransactionRecordModel, TransactionInput> {

    private final TransactionPort transactionPort;

    public ExecuteTransferHandler(TransactionPort transactionPort) {
        this.transactionPort = transactionPort;
    }

    /**
     * @Transactional ensures that if any of the 3 steps fail 
     * (e.g., receiver doesn't exist, or OptimisticLockException occurs),
     * ALL changes are rolled back automatically.
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public TransactionRecordModel handle(TransactionInput input) {
        
        // --- STEP 1: Fetch and update Sender's Wallet ---
        WalletModel senderWallet = transactionPort.getWalletByUserId(input.getSenderUserId());
        senderWallet.deductBalance(input.getAmount());
        transactionPort.updateWallet(senderWallet); // This will check @Version in the DB adapter

        // --- STEP 2: Fetch and update Receiver's Wallet ---
        WalletModel receiverWallet = transactionPort.getWalletByUserId(input.getReceiverUserId());
        receiverWallet.addBalance(input.getAmount());
        transactionPort.updateWallet(receiverWallet); // This will also check @Version

        // --- STEP 3: Create and save Transaction History ---
        TransactionRecordModel record = TransactionRecordModel.builder()
                .id(UUID.randomUUID().toString())
                .senderUserId(input.getSenderUserId())
                .receiverUserId(input.getReceiverUserId())
                .amount(input.getAmount())
                .transactionDate(LocalDateTime.now())
                .status("COMPLETED")
                .build();
                
        return transactionPort.saveTransactionRecord(record);
    }
}