package com.project.transaction.infrastructure.client;

import java.time.LocalDateTime;

public record NotificationResponse(
        String notificationId,
        String eventId,
        String status,
        boolean duplicate,
        LocalDateTime createdAt
) {
}
