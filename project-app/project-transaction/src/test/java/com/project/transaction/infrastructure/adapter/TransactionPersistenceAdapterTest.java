package com.project.transaction.infrastructure.adapter;

import com.project.transaction.domain.model.TransactionRecordModel;
import com.project.transaction.domain.model.WalletModel;
import com.project.transaction.infrastructure.entity.TransactionRecordEntity;
import com.project.transaction.infrastructure.entity.WalletEntity;
import com.project.transaction.infrastructure.mapper.TransactionInfrastructureMapper;
import com.project.transaction.infrastructure.repository.TransactionRecordRepository;
import com.project.transaction.infrastructure.repository.WalletRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionPersistenceAdapterTest {

    @Mock
    private WalletRepository walletRepository;

    @Mock
    private TransactionRecordRepository recordRepository;

    @Mock
    private TransactionInfrastructureMapper mapper;

    @InjectMocks
    private TransactionPersistenceAdapter adapter;

    @Test
    void getWalletByUserId_returnsMappedModel_whenFound() {
        // Arrange
        WalletEntity entity = new WalletEntity();
        entity.setId("we-1");
        entity.setUserId("user-1");
        entity.setBalance(100.0);
        entity.setVersion(1L);

        WalletModel model = WalletModel.builder()
                .id("we-1")
                .userId("user-1")
                .balance(100.0)
                .version(1L)
                .build();

        when(walletRepository.findByUser_IdAndIsActiveTrue("user-1")).thenReturn(Optional.of(entity));
        when(mapper.toWalletModel(entity)).thenReturn(model);

        // Act
        WalletModel result = adapter.getWalletByUserId("user-1");

        // Assert
        assertThat(result).isSameAs(model);
        verify(walletRepository).findByUser_IdAndIsActiveTrue("user-1");
        verify(mapper).toWalletModel(entity);
    }

    @Test
    void getWalletByUserId_throws_whenNotFound() {
        when(walletRepository.findByUser_IdAndIsActiveTrue("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> adapter.getWalletByUserId("missing"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Wallet not found for user");

        verify(walletRepository).findByUser_IdAndIsActiveTrue("missing");
        verifyNoInteractions(mapper);
    }

    @Test
    void updateWallet_mapsEntity_savesAndReturnsMappedModel() {
        WalletModel inputModel = WalletModel.builder()
                .id("w-2")
                .userId("user-2")
                .balance(50.0)
                .version(1L)
                .build();

        WalletEntity entity = new WalletEntity();
        entity.setId("w-2");
        entity.setUserId("user-2");
        entity.setBalance(50.0);

        WalletEntity savedEntity = new WalletEntity();
        savedEntity.setId("w-2");
        savedEntity.setUserId("user-2");
        savedEntity.setBalance(50.0);
        savedEntity.setVersion(2L);

        WalletModel returnedModel = WalletModel.builder()
                .id("w-2")
                .userId("user-2")
                .balance(50.0)
                .version(2L)
                .build();

        when(mapper.toWalletEntity(inputModel)).thenReturn(entity);
        when(walletRepository.save(entity)).thenReturn(savedEntity);
        when(mapper.toWalletModel(savedEntity)).thenReturn(returnedModel);

        WalletModel result = adapter.updateWallet(inputModel);

        assertThat(result).isSameAs(returnedModel);
        verify(mapper).toWalletEntity(inputModel);
        verify(walletRepository).save(entity);
        verify(mapper).toWalletModel(savedEntity);
    }

    @Test
    void findWalletById_whenWalletExists_returnsMappedWallet() {
        WalletEntity entity = new WalletEntity();
        WalletModel model = WalletModel.builder().id("wallet-2").userId("user-2").build();
        when(walletRepository.findByIdAndIsActiveTrue("wallet-2")).thenReturn(Optional.of(entity));
        when(mapper.toWalletModel(entity)).thenReturn(model);

        Optional<WalletModel> result = adapter.findWalletById("wallet-2");

        assertThat(result).contains(model);
        verify(walletRepository).findByIdAndIsActiveTrue("wallet-2");
        verify(mapper).toWalletModel(entity);
        verifyNoInteractions(recordRepository);
    }

    @Test
    void findWalletById_whenWalletDoesNotExist_returnsEmpty() {
        when(walletRepository.findByIdAndIsActiveTrue("missing")).thenReturn(Optional.empty());

        Optional<WalletModel> result = adapter.findWalletById("missing");

        assertThat(result).isEmpty();
        verify(walletRepository).findByIdAndIsActiveTrue("missing");
        verifyNoInteractions(recordRepository, mapper);
    }

    @Test
    void deleteWalletById_marksActiveWalletInactive() {
        WalletEntity wallet = new WalletEntity();
        when(walletRepository.findByIdAndIsActiveTrue("wallet-2")).thenReturn(Optional.of(wallet));
        when(walletRepository.save(wallet)).thenReturn(wallet);

        adapter.deleteWalletById("wallet-2");

        assertThat(wallet.isActive()).isFalse();
        verify(walletRepository).findByIdAndIsActiveTrue("wallet-2");
        verify(walletRepository).save(wallet);
        verifyNoInteractions(recordRepository, mapper);
    }

    @Test
    void deleteWalletById_whenMissing_doesNotSave() {
        when(walletRepository.findByIdAndIsActiveTrue("missing")).thenReturn(Optional.empty());

        adapter.deleteWalletById("missing");

        verify(walletRepository).findByIdAndIsActiveTrue("missing");
        verify(walletRepository, never()).save(any());
        verifyNoInteractions(recordRepository, mapper);
    }

    @Test
    void saveTransactionRecord_maps_andReturnsSavedModel() {
        TransactionRecordModel input = TransactionRecordModel.builder()
                .id(null)
                .senderUserId("s")
                .receiverUserId("r")
                .amount(1.0)
                .transactionDate(LocalDateTime.now())
                .status("COMPLETED")
                .build();

        TransactionRecordEntity entity = new TransactionRecordEntity();
        entity.setId(null);
        entity.setSenderUserId("s");
        entity.setReceiverUserId("r");

        TransactionRecordEntity savedEntity = new TransactionRecordEntity();
        savedEntity.setId("rec-1");
        savedEntity.setSenderUserId("s");
        savedEntity.setReceiverUserId("r");
        savedEntity.setAmount(1.0);

        TransactionRecordModel returned = TransactionRecordModel.builder()
                .id("rec-1")
                .senderUserId("s")
                .receiverUserId("r")
                .amount(1.0)
                .transactionDate(input.getTransactionDate())
                .status("COMPLETED")
                .build();

        when(mapper.toRecordEntity(input)).thenReturn(entity);
        when(recordRepository.save(entity)).thenReturn(savedEntity);
        when(mapper.toRecordModel(savedEntity)).thenReturn(returned);

        TransactionRecordModel result = adapter.saveTransactionRecord(input);

        assertThat(result).isSameAs(returned);
        verify(mapper).toRecordEntity(input);
        verify(recordRepository).save(entity);
        verify(mapper).toRecordModel(savedEntity);
    }

    @Test
    void getTransactionHistory_delegatesToRepository_andMapsEntitiesToModels() {
        String userId = "alice";
        LocalDateTime start = LocalDateTime.of(2025,1,1,0,0);
        LocalDateTime end = LocalDateTime.of(2025,1,2,0,0);
        Pageable pageable = PageRequest.of(0, 10);

        TransactionRecordEntity e1 = new TransactionRecordEntity();
        e1.setId("e1");
        TransactionRecordEntity e2 = new TransactionRecordEntity();
        e2.setId("e2");

        TransactionRecordModel m1 = TransactionRecordModel.builder().id("e1").build();
        TransactionRecordModel m2 = TransactionRecordModel.builder().id("e2").build();

        Page<TransactionRecordEntity> entityPage = new PageImpl<>(List.of(e1, e2), pageable, 2);

        when(recordRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(entityPage);
        when(mapper.toRecordModel(e1)).thenReturn(m1);
        when(mapper.toRecordModel(e2)).thenReturn(m2);

        Page<TransactionRecordModel> result = adapter.getTransactionHistory(userId, start, end, pageable);

        assertThat(result).isNotNull();
        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getContent()).containsExactly(m1, m2);

        verify(recordRepository).findAll(any(Specification.class), eq(pageable));
        verify(mapper).toRecordModel(e1);
        verify(mapper).toRecordModel(e2);
    }

    @Test
    void getSuspiciousTransfers_buildsPairsForHighValueTransactionsFromSameSender() {
        TransactionRecordEntity first = record("tx-1", "alice", 6000.0);
        TransactionRecordEntity second = record("tx-2", "alice", 7000.0);
        TransactionRecordEntity otherSender = record("tx-3", "bob", 8000.0);
        when(recordRepository.findAllByAmountGreaterThan(5000.0))
                .thenReturn(List.of(first, second, otherSender));

        List<Object[]> result = adapter.getSuspiciousTransfers();

        assertThat(result).hasSize(2);
        assertThat(result.get(0)).containsExactly("tx-1", 6000.0, "tx-2", 7000.0);
        assertThat(result.get(1)).containsExactly("tx-2", 7000.0, "tx-1", 6000.0);
        verify(recordRepository).findAllByAmountGreaterThan(5000.0);
        verifyNoMoreInteractions(recordRepository);
        verifyNoInteractions(mapper);
    }

    private TransactionRecordEntity record(String id, String senderUserId, double amount) {
        TransactionRecordEntity entity = new TransactionRecordEntity();
        entity.setId(id);
        entity.setSenderUserId(senderUserId);
        entity.setAmount(amount);
        return entity;
    }
}
