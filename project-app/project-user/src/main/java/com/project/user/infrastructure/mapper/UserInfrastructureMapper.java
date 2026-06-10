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
    @Mapping(target = "version", ignore = true)
    UserEntity toEntity(UserModel model);

    // Entity -> Domain Model
    @Mapping(target = "isActive", source = "active")
    UserModel toModel(UserEntity entity);

}
