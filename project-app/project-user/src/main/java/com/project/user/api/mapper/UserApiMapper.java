package com.project.user.api.mapper;

import com.project.user.api.dto.CreateUserRequest;
import com.project.user.api.dto.CreateUserResponse;
import com.project.user.domain.model.UserCreateInput;
import com.project.user.domain.model.UserModel;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

/**
 * Mapper interface for API layer to convert DTOs to Domain inputs and vice versa.
 */
@Mapper(componentModel = "spring")
public interface UserApiMapper {

    UserApiMapper INSTANCE = Mappers.getMapper(UserApiMapper.class);

    // DTO -> Domain Input
    @Mapping(target = "rawPassword", source = "password")
    UserCreateInput toInput(CreateUserRequest request);

    // Domain Model -> Response DTO
    @Mapping(target = "statusMessage", constant = "User successfully created.")
    CreateUserResponse toResponse(UserModel model);
}