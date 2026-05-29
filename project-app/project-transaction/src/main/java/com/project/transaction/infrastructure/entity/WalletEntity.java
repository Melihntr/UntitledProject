package com.project.transaction.infrastructure.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "wallets")
@Getter
@Setter
public class WalletEntity {

    @Id
    @Column(name = "id", updatable = false, nullable = false)
    private String id;

    @Column(name = "user_id", nullable = false, unique = true)
    private String userId;

    @Column(name = "balance", nullable = false)
    private Double balance;

    // Optimistic Locking: Aynı anda iki işlem buraya yazmaya çalışırsa Spring hata fırlatır!
    @Version
    @Column(name = "version")
    private Long version;
}