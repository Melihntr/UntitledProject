package com.project.transaction.api.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * DTO for outgoing balance transfer responses.
 */
@Getter
@Builder
public class TransferResponse {
    private String transactionId;
    private String status;
    private Double amount;
    private LocalDateTime transactionDate;
}