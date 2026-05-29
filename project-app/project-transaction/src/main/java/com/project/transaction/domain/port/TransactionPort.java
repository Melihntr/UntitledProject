package com.project.transaction.domain.port;

import java.util.List;

import com.project.transaction.domain.model.TransactionRecordModel;
import com.project.transaction.domain.model.WalletModel;

public interface TransactionPort {
    
    WalletModel getWalletByUserId(String userId);
    
    WalletModel updateWallet(WalletModel walletModel);
    
    TransactionRecordModel saveTransactionRecord(TransactionRecordModel recordModel);
    
    org.springframework.data.domain.Page<TransactionRecordModel> getTransactionHistory(
            String userId, java.time.LocalDateTime startDate, java.time.LocalDateTime endDate, org.springframework.data.domain.Pageable pageable);
    List<Object[]> getSuspiciousTransfers();
}