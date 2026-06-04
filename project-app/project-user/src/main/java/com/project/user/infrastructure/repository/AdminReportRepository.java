package com.project.user.infrastructure.repository;

import com.project.user.infrastructure.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AdminReportRepository extends JpaRepository<UserEntity, String> {

    // PROJEKSIYONLAR (Sadece bu sorgulara ozel donus tipleri)
    interface UserWalletSummary {
        String getUsername();
        String getEmail();
        BigDecimal getBalance();
    }

    interface ActiveTransferUser {
        String getUsername();
        BigDecimal getAmount();
        LocalDateTime getCreatedAt();
    }

    interface OrphanWallet {
        String getWalletId();
        BigDecimal getBalance();
        String getSupposedUserId();
    }

    // 1. ENDPOINT: LEFT JOIN (Cüzdanı olan/olmayan tüm kullanıcılar)
    @Query("SELECT u.username AS username, u.email AS email, w.balance AS balance " +
           "FROM UserEntity u LEFT JOIN WalletEntity w ON u.id = w.userId")
    List<UserWalletSummary> findUserWalletSummaries();

 // ENDPOINT 2: INNER JOIN
    @Query("SELECT u.username AS username, t.amount AS amount, t.transactionDate AS createdAt " +
           "FROM UserEntity u INNER JOIN TransactionRecordEntity t ON u.id = t.senderUserId " +
           "WHERE t.status = 'COMPLETED'")
    List<ActiveTransferUser> findActiveTransferUsers();

    // 3. ENDPOINT: RIGHT JOIN veya NATIVE SQL (Sahibi silinmis yetim cuzdanlar)
    // JPA JPQL yapisinda RIGHT JOIN her zaman desteklenmeyebilir, bu gibi kritik 
    // veri butunlugu (Data Integrity) sorgularinda Native SQL kullanmak daha guvenlidir.
    @Query(value = "SELECT w.id AS walletId, w.balance AS balance, w.user_id AS supposedUserId " +
                   "FROM wallets w LEFT JOIN users u ON w.user_id = u.id " +
                   "WHERE u.id IS NULL", nativeQuery = true)
    List<OrphanWallet> findOrphanWallets();
}