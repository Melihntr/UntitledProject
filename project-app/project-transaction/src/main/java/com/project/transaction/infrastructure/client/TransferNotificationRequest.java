package com.project.transaction.infrastructure.client;

import java.math.BigDecimal;

public record TransferNotificationRequest(
        String eventId,
        String type,
        String sourceService,
        String recipientId,
        String title,
        String message,
        String referenceId,
        BigDecimal amount,
        String currency
) {

    private static final String SOURCE_SERVICE = "enterprise-app";
    private static final String TYPE = "TRANSFER_RECEIVED";
    private static final String CURRENCY = "TRY";

    public static TransferNotificationRequest receivedTransfer(
            String transactionId, String recipientId, Double amount) {
        return new TransferNotificationRequest(
                transactionId,
                TYPE,
                SOURCE_SERVICE,
                recipientId,
                "Transfer received",
                "You received a transfer of " + amount + " TRY.",
                transactionId,
                BigDecimal.valueOf(amount),
                CURRENCY
        );
    }
}
