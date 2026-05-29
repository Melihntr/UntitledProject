package com.project.transaction.api.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

/**
 * DTO for incoming balance transfer requests.
 */
@Getter
@Setter
public class TransferRequest {

    @NotBlank(message = "Sender User ID cannot be blank")
    private String senderUserId;

    @NotBlank(message = "Receiver User ID cannot be blank")
    private String receiverUserId;

    @NotNull(message = "Transfer amount is required")
    @DecimalMin(value = "0.01", message = "Transfer amount must be greater than zero")
    private Double amount;
}