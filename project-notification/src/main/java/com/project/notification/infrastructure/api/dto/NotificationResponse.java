package com.project.notification.infrastructure.api.dto;

import com.project.notification.domain.model.NotificationStatus;

import java.time.LocalDateTime;

public record NotificationResponse(
        String notificationId,
        String eventId,
        NotificationStatus status,
        boolean duplicate,
        LocalDateTime createdAt
) {
}
