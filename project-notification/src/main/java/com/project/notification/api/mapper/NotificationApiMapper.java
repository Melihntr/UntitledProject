package com.project.notification.api.mapper;

import com.project.notification.api.dto.NotificationRequest;
import com.project.notification.api.dto.NotificationResponse;
import com.project.notification.domain.model.RecordNotificationInput;
import com.project.notification.domain.model.NotificationResult;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface NotificationApiMapper {

    RecordNotificationInput toInput(NotificationRequest request);

    NotificationResponse toResponse(NotificationResult result);
}
