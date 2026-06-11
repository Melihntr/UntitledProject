package com.project.notification.domain.port;

import com.project.notification.domain.model.NotificationModel;
import com.project.notification.domain.model.NotificationSaveResult;

import java.util.Optional;

public interface NotificationPort {

    Optional<NotificationModel> findByEventId(String eventId);

    NotificationSaveResult save(NotificationModel notification);
}
