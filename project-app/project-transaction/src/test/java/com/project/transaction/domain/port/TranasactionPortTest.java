package com.project.transaction.domain.port;

import com.project.transaction.domain.model.TransactionRecordModel;
import com.project.transaction.domain.model.WalletModel;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TransactionPortTest {

    /**
     * A tiny in-memory implementation of the TransactionPort used to smoke-test
     * the port contract. This mirrors expected production semantics closely enough
     * for unit-testing callers of the port in isolation.
     */
    private final TransactionPort inMemoryPort = new TransactionPort() {

        private final Map<String, WalletModel> wallets = new HashMap<>();
        private final List<TransactionRecordModel> records = new ArrayList<>();

        @Override
        public WalletModel getWalletByUserId(String userId) {
            WalletModel w = wallets.get(userId);
            if (w == null) {
                throw new IllegalArgumentException("Wallet not found for user: " + userId);
            }
            return w;
        }

        @Override
        public WalletModel updateWallet(WalletModel walletModel) {
            wallets.put(walletModel.getUserId(), walletModel);
            return walletModel;
        }

        @Override
        public Optional<WalletModel> findWalletById(String walletId) {
            return wallets.values().stream()
                    .filter(wallet -> walletId.equals(wallet.getId()))
                    .findFirst();
        }

        @Override
        public void deleteWalletById(String walletId) {
            findWalletById(walletId).ifPresent(wallet -> wallets.remove(wallet.getUserId()));
        }

        @Override
        public TransactionRecordModel saveTransactionRecord(TransactionRecordModel recordModel) {
            // assign id if missing to simulate persistence
            TransactionRecordModel toSave = TransactionRecordModel.builder()
                    .id(recordModel.getId() == null ? UUID.randomUUID().toString() : recordModel.getId())
                    .senderUserId(recordModel.getSenderUserId())
                    .receiverUserId(recordModel.getReceiverUserId())
                    .amount(recordModel.getAmount())
                    .transactionDate(recordModel.getTransactionDate())
                    .status(recordModel.getStatus())
                    .build();
            records.add(toSave);
            return toSave;
        }

        @Override
        public org.springframework.data.domain.Page<TransactionRecordModel> getTransactionHistory(String userId, LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {
            List<TransactionRecordModel> filtered = records.stream()
                    .filter(r -> (userId.equals(r.getSenderUserId()) || userId.equals(r.getReceiverUserId())))
                    .filter(r -> {
                        LocalDateTime d = r.getTransactionDate();
                        return (d.isEqual(startDate) || d.isAfter(startDate)) &&
                                (d.isEqual(endDate) || d.isBefore(endDate));
                    })
                    .collect(Collectors.toList());

            int start = (int) pageable.getOffset();
            int end = Math.min(start + pageable.getPageSize(), filtered.size());
            List<TransactionRecordModel> content = start <= end ? filtered.subList(start, end) : Collections.emptyList();

            return new org.springframework.data.domain.PageImpl<>(content, pageable, filtered.size());
        }

        @Override
        public List<Object[]> getSuspiciousTransfers() {
            // return a simple aggregation view: [senderId, count]
            Map<String, Long> counts = records.stream()
                    .collect(Collectors.groupingBy(TransactionRecordModel::getSenderUserId, Collectors.counting()));
            return counts.entrySet().stream()
                    .map(e -> new Object[]{e.getKey(), e.getValue()})
                    .collect(Collectors.toList());
        }
    };

    @Test
    void updateWallet_and_getWalletByUserId_work_asExpected() {
        WalletModel wallet = WalletModel.builder()
                .id("w-1")
                .userId("user-1")
                .balance(100.0)
                .version(1L)
                .build();

        WalletModel saved = inMemoryPort.updateWallet(wallet);
        assertThat(saved).isSameAs(wallet);

        WalletModel fetched = inMemoryPort.getWalletByUserId("user-1");
        assertThat(fetched.getUserId()).isEqualTo("user-1");
        assertThat(fetched.getBalance()).isEqualTo(100.0);
    }

    @Test
    void getWalletByUserId_throws_whenNotFound() {
        assertThatThrownBy(() -> inMemoryPort.getWalletByUserId("non-existent"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Wallet not found for user");
    }

    @Test
    void deleteWalletById_removesOnlyRequestedWallet() {
        WalletModel aliceWallet = WalletModel.builder().id("w1").userId("alice").balance(10.0).build();
        WalletModel bobWallet = WalletModel.builder().id("w2").userId("bob").balance(20.0).build();
        inMemoryPort.updateWallet(aliceWallet);
        inMemoryPort.updateWallet(bobWallet);

        assertThat(inMemoryPort.findWalletById("w1")).contains(aliceWallet);
        inMemoryPort.deleteWalletById("w1");

        assertThatThrownBy(() -> inMemoryPort.getWalletByUserId("alice"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThat(inMemoryPort.getWalletByUserId("bob")).isSameAs(bobWallet);
    }

    @Test
    void deleteWalletById_whenMissing_leavesWalletsUnchanged() {
        WalletModel wallet = WalletModel.builder().id("w1").userId("alice").balance(10.0).build();
        inMemoryPort.updateWallet(wallet);

        inMemoryPort.deleteWalletById("missing");

        assertThat(inMemoryPort.findWalletById("missing")).isEmpty();
        assertThat(inMemoryPort.getWalletByUserId("alice")).isSameAs(wallet);
    }

    @Test
    void saveTransactionRecord_and_getTransactionHistory_return_expected_page() {
        LocalDateTime now = LocalDateTime.now();
        TransactionRecordModel r1 = TransactionRecordModel.builder()
                .senderUserId("alice")
                .receiverUserId("bob")
                .amount(10.0)
                .transactionDate(now.minusHours(1))
                .status("COMPLETED")
                .build();

        TransactionRecordModel r2 = TransactionRecordModel.builder()
                .senderUserId("carol")
                .receiverUserId("alice")
                .amount(5.0)
                .transactionDate(now)
                .status("COMPLETED")
                .build();

        TransactionRecordModel saved1 = inMemoryPort.saveTransactionRecord(r1);
        TransactionRecordModel saved2 = inMemoryPort.saveTransactionRecord(r2);

        // query alice's history covering both records time window
        LocalDateTime start = now.minusDays(1);
        LocalDateTime end = now.plusDays(1);
        Page<TransactionRecordModel> page = inMemoryPort.getTransactionHistory("alice", start, end, PageRequest.of(0, 10));

        assertThat(page.getTotalElements()).isEqualTo(2);
        assertThat(page.getContent()).containsExactlyInAnyOrder(saved1, saved2);
    }

    @Test
    void getSuspiciousTransfers_returns_aggregation_like_structure() {
        LocalDateTime now = LocalDateTime.now();
        inMemoryPort.saveTransactionRecord(TransactionRecordModel.builder()
                .senderUserId("alice").receiverUserId("bob").amount(1.0).transactionDate(now).status("COMPLETED").build());
        inMemoryPort.saveTransactionRecord(TransactionRecordModel.builder()
                .senderUserId("alice").receiverUserId("carol").amount(2.0).transactionDate(now).status("COMPLETED").build());
        inMemoryPort.saveTransactionRecord(TransactionRecordModel.builder()
                .senderUserId("bob").receiverUserId("alice").amount(3.0).transactionDate(now).status("COMPLETED").build());

        List<Object[]> suspicious = inMemoryPort.getSuspiciousTransfers();

        // Expect at least two entries (alice and bob) and counts reflect inserted records
        Map<String, Long> map = suspicious.stream()
                .collect(Collectors.toMap(arr -> (String) arr[0], arr -> (Long) arr[1]));

        assertThat(map.get("alice")).isEqualTo(2L);
        assertThat(map.get("bob")).isEqualTo(1L);
    }
}
