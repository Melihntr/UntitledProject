package com.project.notification.infrastructure.mapper;

import com.project.notification.domain.model.NotificationModel;
import com.project.notification.infrastructure.entity.NotificationEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface NotificationInfrastructureMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    NotificationEntity toEntity(NotificationModel model);

    NotificationModel toModel(NotificationEntity entity);
}
