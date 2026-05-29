package com.project.transaction.infrastructure.adapter;

import com.project.transaction.domain.model.TransactionRecordModel;
import com.project.transaction.domain.model.WalletModel;
import com.project.transaction.domain.port.TransactionPort;
import com.project.transaction.infrastructure.entity.TransactionRecordEntity;
import com.project.transaction.infrastructure.entity.WalletEntity;
import com.project.transaction.infrastructure.mapper.TransactionInfrastructureMapper;
import com.project.transaction.infrastructure.repository.TransactionRecordRepository;
import com.project.transaction.infrastructure.repository.WalletRepository;

import java.util.List;

import org.springframework.stereotype.Component;

@Component
public class TransactionPersistenceAdapter implements TransactionPort {

    private final WalletRepository walletRepository;
    private final TransactionRecordRepository recordRepository;
    private final TransactionInfrastructureMapper mapper;

    public TransactionPersistenceAdapter(WalletRepository walletRepository, 
                                         TransactionRecordRepository recordRepository, 
                                         TransactionInfrastructureMapper mapper) {
        this.walletRepository = walletRepository;
        this.recordRepository = recordRepository;
        this.mapper = mapper;
    }

    @Override
    public WalletModel getWalletByUserId(String userId) {
        WalletEntity entity = walletRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("Wallet not found for user: " + userId));
        return mapper.toWalletModel(entity);
    }

    @Override
    public WalletModel updateWallet(WalletModel walletModel) {
        WalletEntity entity = mapper.toWalletEntity(walletModel);
        WalletEntity savedEntity = walletRepository.save(entity);
        return mapper.toWalletModel(savedEntity);
    }

    @Override
    public TransactionRecordModel saveTransactionRecord(TransactionRecordModel recordModel) {
        TransactionRecordEntity entity = mapper.toRecordEntity(recordModel);
        TransactionRecordEntity savedEntity = recordRepository.save(entity);
        return mapper.toRecordModel(savedEntity);
    }
    @Override
    public org.springframework.data.domain.Page<TransactionRecordModel> getTransactionHistory(
            String userId, java.time.LocalDateTime startDate, java.time.LocalDateTime endDate, org.springframework.data.domain.Pageable pageable) {
        
        return recordRepository.findUserTransactionsWithDateFilter(userId, startDate, endDate, pageable)
                .map(mapper::toRecordModel);
    }
    @Override
    public List<Object[]> getSuspiciousTransfers() {
        return recordRepository.findSuspiciousTransfersWithSelfJoin();
    }
}