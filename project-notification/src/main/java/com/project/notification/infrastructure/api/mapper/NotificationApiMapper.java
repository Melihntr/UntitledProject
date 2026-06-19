package com.project.notification.infrastructure.api.mapper;

import com.project.notification.infrastructure.api.dto.NotificationRequest;
import com.project.notification.infrastructure.api.dto.NotificationResponse;
import com.project.notification.domain.usecase.RecordNotificationInput;
import com.project.notification.domain.model.NotificationResult;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface NotificationApiMapper {

    RecordNotificationInput toInput(NotificationRequest request);

    NotificationResponse toResponse(NotificationResult result);
}
