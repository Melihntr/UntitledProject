package com.project.transaction.domain.model;

public record NotificationResult(
        String notificationId,
        String eventId,
        String status,
        boolean duplicate
) {
}
