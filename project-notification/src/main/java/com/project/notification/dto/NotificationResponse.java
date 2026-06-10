package com.project.notification.dto;

import com.project.notification.entity.NotificationStatus;

import java.time.LocalDateTime;

public record NotificationResponse(
        String notificationId,
        String eventId,
        NotificationStatus status,
        boolean duplicate,
        LocalDateTime createdAt
) {
}
