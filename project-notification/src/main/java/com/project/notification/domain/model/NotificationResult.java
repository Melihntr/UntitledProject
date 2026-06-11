package com.project.notification.domain.model;

import java.time.LocalDateTime;

public record NotificationResult(
        String notificationId,
        String eventId,
        NotificationStatus status,
        boolean duplicate,
        LocalDateTime createdAt
) {

    public static NotificationResult from(NotificationModel notification, boolean duplicate) {
        return new NotificationResult(
                notification.id(),
                notification.eventId(),
                notification.status(),
                duplicate,
                notification.createdAt()
        );
    }
}
