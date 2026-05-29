package com.project.transaction.api.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * Data Transfer Object (DTO) for outgoing balance transfer responses.
 * Acts as the presentation layer contract, returning the result of a financial transaction
 * to the API client without exposing the internal domain models or database entities.
 */
@Getter
@Builder
public class TransferResponse {

    /**
     * The unique identifier for the completed transaction.
     * Used by the client for tracking, auditing, and receipt generation.
     */
    private String transactionId;

    /**
     * The final state of the transaction execution (e.g., "SUCCESS", "FAILED", "REJECTED").
     */
    private String status;

    /**
     * The monetary amount that was processed during this transaction.
     */
    private Double amount;

    /**
     * The exact timestamp indicating when the transaction was finalized in the system.
     */
    private LocalDateTime transactionDate;
}