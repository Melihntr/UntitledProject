package com.project.user.infrastructure.mapper;

import com.project.user.domain.model.UserModel;
import com.project.user.infrastructure.entity.UserEntity;
import org.mapstruct.Mapper;

/**
 * Mapper interface for the Infrastructure layer.
 * Converts Core Domain Models to Database Entities and vice versa.
 */
@Mapper(componentModel = "spring")
public interface UserInfrastructureMapper {

    // Domain Model -> Entity
    UserEntity toEntity(UserModel model);

    // Entity -> Domain Model
    UserModel toModel(UserEntity entity);

}