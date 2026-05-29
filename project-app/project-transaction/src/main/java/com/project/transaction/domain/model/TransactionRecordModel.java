package com.project.transaction.domain.model;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * Domain model representing a historical transaction record.
 */
@Getter
@Builder
public class TransactionRecordModel {
    private String id;
    private String senderUserId;
    private String receiverUserId;
    private Double amount;
    private LocalDateTime transactionDate;
    private String status;
}