package com.project.notification.domain.model;

import com.project.notification.domain.usecase.RecordNotificationInput;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record NotificationModel(
        String id,
        String eventId,
        NotificationType type,
        String sourceService,
        String recipientId,
        String title,
        String message,
        String referenceId,
        BigDecimal amount,
        String currency,
        NotificationStatus status,
        LocalDateTime createdAt
) {

    public static NotificationModel from(RecordNotificationInput input) {
        return new NotificationModel(
                null,
                input.eventId(),
                input.type(),
                input.sourceService(),
                input.recipientId(),
                input.title(),
                input.message(),
                input.referenceId(),
                input.amount(),
                input.currency(),
                NotificationStatus.RECORDED,
                null
        );
    }
}
