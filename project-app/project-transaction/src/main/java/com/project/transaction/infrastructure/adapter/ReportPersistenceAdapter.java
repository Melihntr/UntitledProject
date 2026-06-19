package com.project.transaction.infrastructure.adapter;

import com.project.transaction.infrastructure.entity.WalletEntity;
import com.project.transaction.infrastructure.repository.TransactionRecordRepository;
import com.project.transaction.infrastructure.repository.WalletRepository;
import com.project.user.domain.model.ActiveTransferUserModel;
import com.project.user.domain.model.OrphanWalletModel;
import com.project.user.domain.model.UserWalletSummaryModel;
import com.project.user.domain.port.ReportPort;
import com.project.user.infrastructure.entity.UserEntity;
import com.project.user.infrastructure.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ReportPersistenceAdapter implements ReportPort {

    private final UserRepository userRepository;
    private final WalletRepository walletRepository;
    private final TransactionRecordRepository transactionRecordRepository;

    @Override
    public List<UserWalletSummaryModel> getUserWalletSummaries() {
        Map<String, WalletEntity> walletsByUserId = activeWalletsByUserId();
        return userRepository.findAllByIsUserDeletedFalse().stream()
                .map(user -> UserWalletSummaryModel.builder()
                        .username(user.getUsername())
                        .email(user.getEmail())
                        .balance(toBigDecimal(walletsByUserId.get(user.getId())))
                        .build())
                .toList();
    }

    @Override
    public List<ActiveTransferUserModel> getActiveTransferUsers() {
        Map<String, UserEntity> activeUsersById = activeUsersById();
        return transactionRecordRepository.findAllByStatus("COMPLETED").stream()
                .filter(transaction -> activeUsersById.containsKey(transaction.getSenderUserId()))
                .map(transaction -> ActiveTransferUserModel.builder()
                        .username(activeUsersById.get(transaction.getSenderUserId()).getUsername())
                        .amount(BigDecimal.valueOf(transaction.getAmount()))
                        .createdAt(transaction.getTransactionDate())
                        .build())
                .toList();
    }

    @Override
    public List<OrphanWalletModel> getOrphanWallets() {
        Map<String, UserEntity> activeUsersById = activeUsersById();
        return walletRepository.findAllByIsActiveTrue().stream()
                .filter(wallet -> !activeUsersById.containsKey(wallet.getUserId()))
                .map(wallet -> OrphanWalletModel.builder()
                        .walletId(wallet.getId())
                        .balance(BigDecimal.valueOf(wallet.getBalance()))
                        .supposedUserId(wallet.getUserId())
                        .build())
                .toList();
    }

    private Map<String, WalletEntity> activeWalletsByUserId() {
        return walletRepository.findAllByIsActiveTrue().stream()
                .collect(Collectors.toMap(WalletEntity::getUserId, Function.identity()));
    }

    private Map<String, UserEntity> activeUsersById() {
        return userRepository.findAllByIsUserDeletedFalse().stream()
                .collect(Collectors.toMap(UserEntity::getId, Function.identity()));
    }

    private BigDecimal toBigDecimal(WalletEntity wallet) {
        return wallet == null ? null : BigDecimal.valueOf(wallet.getBalance());
    }
}
