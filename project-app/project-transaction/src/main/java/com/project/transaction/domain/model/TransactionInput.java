package com.project.transaction.domain.model;

import lombok.Builder;
import lombok.Getter;

/**
 * Domain input model representing a requested financial transaction.
 * This model resides in the core domain layer, fully decoupled from external API contracts (DTOs)
 * and infrastructure details. It encapsulates the exact data required by the business use case 
 * to execute a balance transfer.
 */
@Getter
@Builder
public class TransactionInput {

    /**
     * The unique identifier of the user initiating the transaction (the sender).
     * By the time this reaches the domain layer, authorization should already be validated.
     */
    private String senderUserId;

    /**
     * The unique identifier of the user intended to receive the funds.
     */
    private String receiverUserId;

    /**
     * The monetary amount to be transferred from the sender's wallet to the receiver's wallet.
     */
    private Double amount;
}