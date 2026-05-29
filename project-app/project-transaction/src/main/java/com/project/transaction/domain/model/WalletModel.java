package com.project.transaction.domain.model;

import lombok.Builder;
import lombok.Getter;

/**
 * Domain model representing a User's Wallet.
 */
@Getter
@Builder
public class WalletModel {
    private String id;
    private String userId;
    private Double balance;
    
    // Optimistic locking field carried over from the database entity
    private Long version;

    // Business method to deduct balance
    public void deductBalance(Double amount) {
        if (this.balance < amount) {
            throw new IllegalArgumentException("Insufficient balance");
        }
        this.balance -= amount;
    }

    // Business method to add balance
    public void addBalance(Double amount) {
        this.balance += amount;
    }
}