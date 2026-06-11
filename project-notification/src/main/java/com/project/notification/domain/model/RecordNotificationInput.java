package com.project.notification.domain.model;

import java.math.BigDecimal;

public record RecordNotificationInput(
        String eventId,
        NotificationType type,
        String sourceService,
        String recipientId,
        String title,
        String message,
        String referenceId,
        BigDecimal amount,
        String currency
) {
}
