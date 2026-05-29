package com.project.transaction.api.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

/**
 * Data Transfer Object (DTO) for incoming balance transfer requests.
 * Acts as the presentation layer contract, capturing and validating the JSON payload
 * sent by the API client before it is mapped to the internal core domain model.
 */
@Getter
@Setter
public class TransferRequest {

    /**
     * The unique identifier of the user initiating the transfer.
     * At the controller level, this is validated against the authenticated user's header ID
     * to prevent unauthorized transactions.
     */
    @NotBlank(message = "Sender User ID cannot be blank")
    private String senderUserId;

    /**
     * The unique identifier of the user receiving the funds.
     */
    @NotBlank(message = "Receiver User ID cannot be blank")
    private String receiverUserId;

    /**
     * The monetary amount to be transferred between the wallets.
     * * Enterprise Note: While Double is used here for simplicity, in a strict 
     * financial production environment, java.math.BigDecimal is the industry standard 
     * to prevent floating-point arithmetic precision loss.
     */
    @NotNull(message = "Transfer amount is required")
    @DecimalMin(value = "0.01", message = "Transfer amount must be greater than zero")
    private Double amount;
}