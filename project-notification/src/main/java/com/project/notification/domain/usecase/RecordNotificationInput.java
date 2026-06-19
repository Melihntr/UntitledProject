package com.project.notification.domain.usecase;

import com.project.notification.domain.model.NotificationType;

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
