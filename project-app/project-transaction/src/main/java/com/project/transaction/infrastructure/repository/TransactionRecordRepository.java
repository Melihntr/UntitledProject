package com.project.transaction.infrastructure.repository;

import com.project.transaction.infrastructure.entity.TransactionRecordEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TransactionRecordRepository extends JpaRepository<TransactionRecordEntity, String> {

    /**
     * MEVCUT SORGUMUZ: OR Şartlı, Tarih Filtreli ve Sayfalamalı (Pagination)
     * İşlem geçmişini sayfa sayfa çekmek için kullanılır.
     */
    @Query("SELECT t FROM TransactionRecordEntity t " +
           "WHERE (t.senderUserId = :userId OR t.receiverUserId = :userId) " +
           "AND t.transactionDate >= :startDate " +
           "AND t.transactionDate <= :endDate")
    Page<TransactionRecordEntity> findUserTransactionsWithDateFilter(
            @Param("userId") String userId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable);


    /**
     * 1. INNER JOIN (Kesin Kesişim)
     * Sadece her iki tabloda da (User ve Wallet) eşleşenleri getirir.
     * Business Senaryosu: Bakiyesi belirli bir tutarın üzerinde olan VIP kullanıcıların adlarını getirme.
     */
    @Query("SELECT u.username, w.balance FROM UserEntity u " +
           "INNER JOIN WalletEntity w ON u.id = w.userId " +
           "WHERE w.balance > :minBalance")
    List<Object[]> findVipUsersWithInnerJoin(@Param("minBalance") Double minBalance);


    /**
     * 2. LEFT JOIN (Sol Tablonun Tamamı)
     * Sol tablodaki (User) her şeyi getirir, sağdakinde eşleşme yoksa (Cüzdanı yoksa) NULL döner.
     * Business Senaryosu: Tüm kullanıcı listesini çekmek, cüzdanı olmayanların bakiyesine NULL yazıp
     * onlara "Cüzdan Oluşturun" bildirimi atmak için raporlama.
     */
    @Query("SELECT u.username, w.balance FROM UserEntity u " +
           "LEFT JOIN WalletEntity w ON u.id = w.userId")
    List<Object[]> findAllUsersAndBalancesLeftJoin();


    /**
     * 3. RIGHT JOIN (Sağ Tablonun Tamamı)
     * Sağ tablodaki (TransactionRecord) her şeyi getirir, sol (User) silinmişse NULL döner.
     * Business Senaryosu: Kullanıcı hesabı kapatıp sistemden silinse bile, mali kayıtları
     * kaybetmemek ve geçmişteki transferleri listelemek.
     */
    @Query("SELECT t.id, t.amount, u.username FROM UserEntity u " +
           "RIGHT JOIN TransactionRecordEntity t ON u.id = t.senderUserId")
    List<Object[]> findAllTransactionsEvenIfUserDeletedRightJoin();


    /**
     * 4. SELF JOIN (Tablonun Kendisiyle Birleştirilmesi)
     * Business Senaryosu: Fraud (Dolandırıcılık / Kara Para Aklama) Tespiti.
     * Aynı kişinin (senderUserId), yaptığı farklı iki yüksek tutarlı transferi aynı satıra getirir
     * ki arka arkaya şüpheli bir işlem yapılıp yapılmadığı tespit edilebilsin.
     */
    @Query("SELECT t1.id, t1.amount, t2.id, t2.amount FROM TransactionRecordEntity t1 " +
           "INNER JOIN TransactionRecordEntity t2 ON t1.senderUserId = t2.senderUserId " +
           "WHERE t1.id <> t2.id AND t1.amount > 5000 AND t2.amount > 5000")
    List<Object[]> findSuspiciousTransfersWithSelfJoin();

}