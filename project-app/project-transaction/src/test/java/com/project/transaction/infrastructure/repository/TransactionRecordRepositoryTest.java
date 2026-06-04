package com.project.transaction.infrastructure.repository;

import com.project.transaction.infrastructure.entity.TransactionRecordEntity;
import com.project.transaction.infrastructure.entity.WalletEntity;
import com.project.user.infrastructure.entity.UserEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.*;
import org.springframework.test.annotation.Rollback;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = Replace.ANY)
class TransactionRecordRepositoryTest {

    @Autowired
    private TransactionRecordRepository transactionRecordRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    @Rollback
    void findUserTransactionsWithDateFilter_returnsOnlyUserRelatedRecords_inDateRange() {
        // Prepare data
        LocalDateTime now = LocalDateTime.now();
        TransactionRecordEntity t1 = new TransactionRecordEntity("tx-1", "alice", "bob", 10.0, now.minusDays(2), "COMPLETED");
        TransactionRecordEntity t2 = new TransactionRecordEntity("tx-2", "carol", "alice", 5.0, now.minusDays(1), "COMPLETED");
        TransactionRecordEntity t3 = new TransactionRecordEntity("tx-3", "alice", "dave", 7.0, now.minusDays(10), "COMPLETED"); // outside window

        entityManager.persist(t1);
        entityManager.persist(t2);
        entityManager.persist(t3);
        entityManager.flush();

        Pageable pageable = PageRequest.of(0, 10, Sort.by("transactionDate").descending());
        LocalDateTime start = now.minusDays(3);
        LocalDateTime end = now;

        Page<TransactionRecordEntity> page = transactionRecordRepository.findUserTransactionsWithDateFilter("alice", start, end, pageable);

        assertThat(page).isNotNull();
        assertThat(page.getTotalElements()).isEqualTo(2);
        assertThat(page.getContent()).extracting(TransactionRecordEntity::getId).containsExactlyInAnyOrder("tx-1", "tx-2");
    }

    @Test
    @Rollback
    void findVipUsersWithInnerJoin_returnsOnlyUsersWithWalletBalanceAboveThreshold() {
        // Prepare users and wallets
        UserEntity u1 = new UserEntity();
        u1.setId("user-1");
        u1.setUsername("rich_alice");
        entityManager.persist(u1);

        WalletEntity w1 = new WalletEntity("w-1", "user-1", 10_000.0, 0L);
        entityManager.persist(w1);

        UserEntity u2 = new UserEntity();
        u2.setId("user-2");
        u2.setUsername("poor_bob");
        entityManager.persist(u2);

        WalletEntity w2 = new WalletEntity("w-2", "user-2", 50.0, 0L);
        entityManager.persist(w2);

        entityManager.flush();

        List<Object[]> results = transactionRecordRepository.findVipUsersWithInnerJoin(1000.0);

        assertThat(results).isNotNull();
        // Expect only rich_alice row
        assertThat(results).hasSize(1);
        Object[] row = results.get(0);
        assertThat(row[0]).isEqualTo("rich_alice");
        assertThat(row[1]).isEqualTo(10000.0);
    }

    @Test
    @Rollback
    void findAllUsersAndBalancesLeftJoin_includesUsersWithoutWallets_withNullBalance() {
        // User with wallet
        UserEntity u1 = new UserEntity();
        u1.setId("u-a");
        u1.setUsername("has_wallet");
        entityManager.persist(u1);
        WalletEntity w1 = new WalletEntity("we-a", "u-a", 200.0, 0L);
        entityManager.persist(w1);

        // User without wallet
        UserEntity u2 = new UserEntity();
        u2.setId("u-b");
        u2.setUsername("no_wallet");
        entityManager.persist(u2);

        entityManager.flush();

        List<Object[]> results = transactionRecordRepository.findAllUsersAndBalancesLeftJoin();

        // There should be at least two rows, find the one for 'no_wallet' and assert balance is null
        Optional<Object[]> noWalletRow = results.stream()
                .filter(r -> "no_wallet".equals(r[0]))
                .findAny();

        assertThat(noWalletRow).isPresent();
        Object[] row = noWalletRow.get();
        // username present, balance null
        assertThat(row[0]).isEqualTo("no_wallet");
        assertThat(row[1]).isNull();
    }

    @Test
    @Rollback
    void findAllTransactionsEvenIfUserDeletedRightJoin_returnsTransactions_withNullUsernamesWhenUserMissing() {
        // Persist a transaction whose senderUserId has no corresponding UserEntity
        TransactionRecordEntity orphan = new TransactionRecordEntity("tx-orphan", "missing-user", "r", 12.0, LocalDateTime.now(), "COMPLETED");
        entityManager.persist(orphan);

        // Also persist a transaction with an existing user
        UserEntity u = new UserEntity();
        u.setId("present-user");
        u.setUsername("present");
        entityManager.persist(u);

        TransactionRecordEntity tWithUser = new TransactionRecordEntity("tx-with-user", "present-user", "x", 8.0, LocalDateTime.now(), "COMPLETED");
        entityManager.persist(tWithUser);

        entityManager.flush();

        List<Object[]> results = transactionRecordRepository.findAllTransactionsEvenIfUserDeletedRightJoin();

        // Find rows by transaction id
        Optional<Object[]> orphanRow = results.stream().filter(r -> "tx-orphan".equals(r[0])).findAny();
        Optional<Object[]> presentRow = results.stream().filter(r -> "tx-with-user".equals(r[0])).findAny();

        assertThat(orphanRow).isPresent();
        Object[] or = orphanRow.get();
        // or[0] = t.id, or[1] = t.amount, or[2] = u.username (expected null for orphan)
        assertThat(or[0]).isEqualTo("tx-orphan");
        assertThat(or[2]).isNull();

        assertThat(presentRow).isPresent();
        Object[] pr = presentRow.get();
        assertThat(pr[0]).isEqualTo("tx-with-user");
        assertThat(pr[2]).isEqualTo("present");
    }

    @Test
    @Rollback
    void findSuspiciousTransfersWithSelfJoin_detectsPairsOfLargeTransactions_fromSameSender() {
        LocalDateTime now = LocalDateTime.now();

        // Two high-value transactions from same sender (should be picked up)
        TransactionRecordEntity a1 = new TransactionRecordEntity("s1-t1", "s1", "r1", 6000.0, now.minusHours(1), "COMPLETED");
        TransactionRecordEntity a2 = new TransactionRecordEntity("s1-t2", "s1", "r2", 7000.0, now.minusHours(2), "COMPLETED");

        // High-value from different sender (should not pair with s1)
        TransactionRecordEntity b1 = new TransactionRecordEntity("s2-t1", "s2", "r3", 8000.0, now, "COMPLETED");

        entityManager.persist(a1);
        entityManager.persist(a2);
        entityManager.persist(b1);
        entityManager.flush();

        List<Object[]> results = transactionRecordRepository.findSuspiciousTransfersWithSelfJoin();

        // Each pair of (t1,t2) for s1 may produce two rows depending on join direction.
        // Ensure that we have at least one row containing both s1-t1 and s1-t2 ids/amounts.
        boolean foundPair = results.stream().anyMatch(r ->
                ("s1-t1".equals(r[0]) && Double.valueOf(6000.0).equals(r[1]) && "s1-t2".equals(r[2]) && Double.valueOf(7000.0).equals(r[3]))
                        || ("s1-t2".equals(r[0]) && Double.valueOf(7000.0).equals(r[1]) && "s1-t1".equals(r[2]) && Double.valueOf(6000.0).equals(r[3]))
        );

        assertThat(foundPair).isTrue();
    }
}
