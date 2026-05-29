package com.project.user.domain.usecase;

import com.project.common.usecase.UseCaseHandler;
import com.project.user.domain.model.UserCreateInput;
import com.project.user.domain.model.UserModel;
import com.project.user.domain.port.UserPort;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * The core business logic handler for creating a new user.
 */
public class CreateUserHandler implements UseCaseHandler<UserModel, UserCreateInput> {

    private final UserPort userPort;

    // Dependency Injection via constructor
    public CreateUserHandler(UserPort userPort) {
        this.userPort = userPort;
    }

    @Override
    public UserModel handle(UserCreateInput input) {
        
        // 1. Map the validated Input to our Core Domain Model
        UserModel newUser = UserModel.builder()
                .id(UUID.randomUUID().toString())
                .username(input.getUsername())
                .email(input.getEmail())
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .build();

        // 2. Persist the domain model via infrastructure port
        // The handler doesn't care if this saves to H2, PostgreSQL, or a file
        return userPort.save(newUser);
    }
}