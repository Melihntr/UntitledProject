package com.project.transaction.infrastructure.adapter;

import com.project.transaction.infrastructure.entity.TransactionRecordEntity;
import com.project.transaction.infrastructure.entity.WalletEntity;
import com.project.transaction.infrastructure.repository.TransactionRecordRepository;
import com.project.transaction.infrastructure.repository.WalletRepository;
import com.project.user.domain.model.ActiveTransferUserModel;
import com.project.user.domain.model.OrphanWalletModel;
import com.project.user.domain.model.UserWalletSummaryModel;
import com.project.user.infrastructure.entity.UserEntity;
import com.project.user.infrastructure.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReportPersistenceAdapterTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private WalletRepository walletRepository;

    @Mock
    private TransactionRecordRepository transactionRecordRepository;

    @InjectMocks
    private ReportPersistenceAdapter adapter;

    @Test
    void getUserWalletSummaries_returnsActiveUsersWithOptionalWalletBalance() {
        UserEntity alice = user("user-1", "alice", "alice@example.com");
        UserEntity bob = user("user-2", "bob", "bob@example.com");
        WalletEntity wallet = wallet("wallet-1", "user-1", 125.50);
        when(userRepository.findAllByIsUserDeletedFalse()).thenReturn(List.of(alice, bob));
        when(walletRepository.findAllByIsActiveTrue()).thenReturn(List.of(wallet));

        List<UserWalletSummaryModel> result = adapter.getUserWalletSummaries();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getUsername()).isEqualTo("alice");
        assertThat(result.get(0).getBalance()).isEqualByComparingTo(BigDecimal.valueOf(125.50));
        assertThat(result.get(1).getUsername()).isEqualTo("bob");
        assertThat(result.get(1).getBalance()).isNull();
    }

    @Test
    void getActiveTransferUsers_returnsOnlyCompletedTransfersFromActiveSenders() {
        LocalDateTime createdAt = LocalDateTime.of(2026, 6, 14, 12, 0);
        UserEntity alice = user("user-1", "alice", "alice@example.com");
        TransactionRecordEntity activeSenderTransaction = transaction("tx-1", "user-1", 10.0, createdAt);
        TransactionRecordEntity inactiveSenderTransaction = transaction("tx-2", "user-2", 20.0, createdAt);
        when(userRepository.findAllByIsUserDeletedFalse()).thenReturn(List.of(alice));
        when(transactionRecordRepository.findAllByStatus("COMPLETED"))
                .thenReturn(List.of(activeSenderTransaction, inactiveSenderTransaction));

        List<ActiveTransferUserModel> result = adapter.getActiveTransferUsers();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getUsername()).isEqualTo("alice");
        assertThat(result.get(0).getAmount()).isEqualByComparingTo(BigDecimal.TEN);
        assertThat(result.get(0).getCreatedAt()).isEqualTo(createdAt);
    }

    @Test
    void getOrphanWallets_returnsOnlyActiveWalletsWithoutActiveOwner() {
        UserEntity alice = user("user-1", "alice", "alice@example.com");
        WalletEntity owned = wallet("wallet-1", "user-1", 10.0);
        WalletEntity orphan = wallet("wallet-2", "deleted-user", 20.0);
        when(userRepository.findAllByIsUserDeletedFalse()).thenReturn(List.of(alice));
        when(walletRepository.findAllByIsActiveTrue()).thenReturn(List.of(owned, orphan));

        List<OrphanWalletModel> result = adapter.getOrphanWallets();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getWalletId()).isEqualTo("wallet-2");
        assertThat(result.get(0).getBalance()).isEqualByComparingTo(BigDecimal.valueOf(20.0));
        assertThat(result.get(0).getSupposedUserId()).isEqualTo("deleted-user");
    }

    private UserEntity user(String id, String username, String email) {
        UserEntity entity = new UserEntity();
        entity.setId(id);
        entity.setUsername(username);
        entity.setEmail(email);
        return entity;
    }

    private WalletEntity wallet(String id, String userId, double balance) {
        WalletEntity entity = new WalletEntity();
        entity.setId(id);
        entity.setUserId(userId);
        entity.setBalance(balance);
        return entity;
    }

    private TransactionRecordEntity transaction(String id, String senderUserId, double amount, LocalDateTime createdAt) {
        TransactionRecordEntity entity = new TransactionRecordEntity();
        entity.setId(id);
        entity.setSenderUserId(senderUserId);
        entity.setAmount(amount);
        entity.setTransactionDate(createdAt);
        return entity;
    }
}
