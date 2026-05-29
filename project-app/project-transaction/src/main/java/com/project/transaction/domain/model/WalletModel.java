package com.project.transaction.domain.model;

import lombok.Builder;
import lombok.Getter;

/**
 * Domain model representing a user's financial wallet.
 * This class acts as a Rich Domain Model within the Clean Architecture, encapsulating 
 * both the state (balance) and the core business rules (deducting/adding funds) 
 * required for wallet operations.
 */
@Getter
@Builder
public class WalletModel {

    /**
     * The unique identifier of the wallet.
     */
    private String id;

    /**
     * The unique identifier of the user who owns this wallet.
     */
    private String userId;

    /**
     * The current available monetary balance in the wallet.
     * * Enterprise Note: While Double is used here for simplicity, java.math.BigDecimal 
     * is the industry standard for financial systems to prevent floating-point precision loss.
     */
    private Double balance;
    
    /**
     * Optimistic locking version field carried over from the infrastructure layer (database entity).
     * Used to prevent race conditions (e.g., double-spending) when multiple concurrent 
     * transfer requests attempt to modify the same wallet simultaneously.
     */
    private Long version;

    /**
     * Core business logic to deduct a specific amount from the wallet.
     * Enforces the domain invariant that a wallet's balance cannot drop below zero.
     *
     * @param amount The monetary amount to be deducted.
     * @throws IllegalArgumentException if the requested amount exceeds the available balance.
     */
    public void deductBalance(Double amount) {
        if (this.balance < amount) {
            // Note: If you have imported it into this module, throwing your custom 
            // com.project.common.exception.BusinessException here is highly recommended.
            throw new IllegalArgumentException("Business Rule Violation: Insufficient balance in the wallet.");
        }
        this.balance -= amount;
    }

    /**
     * Core business logic to add a specific amount to the wallet.
     *
     * @param amount The monetary amount to be deposited.
     */
    public void addBalance(Double amount) {
        this.balance += amount;
    }
}