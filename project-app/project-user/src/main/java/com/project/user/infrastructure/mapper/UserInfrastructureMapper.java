package com.project.user.infrastructure.mapper;

import com.project.user.domain.model.UserModel;
import com.project.user.infrastructure.entity.UserEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * Mapper interface for the Infrastructure layer.
 * Converts Core Domain Models to Database Entities and vice versa.
 */
@Mapper(componentModel = "spring")
public interface UserInfrastructureMapper {

    // Domain Model -> Entity
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "userDeleted", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdTraceId", ignore = true)
    @Mapping(target = "updatedTraceId", ignore = true)
    @Mapping(target = "version", ignore = true)
    UserEntity toEntity(UserModel model);

    // Entity -> Domain Model
    @Mapping(target = "isActive", expression = "java(!entity.isUserDeleted())")
    UserModel toModel(UserEntity entity);

}
