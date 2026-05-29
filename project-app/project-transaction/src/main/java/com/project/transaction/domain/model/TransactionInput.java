package com.project.transaction.domain.model;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TransactionInput {
    private String senderUserId;
    private String receiverUserId;
    private Double amount;
}