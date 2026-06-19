package com.project.user.domain.handler;

import com.project.common.domain.usecase.UseCaseHandler;
import com.project.user.domain.usecase.UserCreateInput;
import com.project.user.domain.model.UserModel;
import com.project.user.domain.port.UserEventPublisherPort;
import com.project.user.domain.port.UserPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * Core business use case handler responsible for registering a new user.
 * This class orchestrates the domain logic, ensuring that the input is transformed 
 * into a valid domain model before delegating persistence to the infrastructure layer.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class CreateUserHandler implements UseCaseHandler<UserModel, UserCreateInput> {

    private final UserPort userPort;
    private final UserEventPublisherPort eventPublisherPort;

    /**
     * Executes the user registration business logic.
     *
     * @param input The validated domain input containing username, email, and the raw password.
     * @return The finalized and persisted user domain model.
     */
    @Override
    public UserModel handle(UserCreateInput input) {
        
        log.info("user.create.request username={}", input.getUsername());

        UserModel newUser = UserModel.builder()
                .username(input.getUsername())
                .email(input.getEmail())
                .createdAt(LocalDateTime.now())
                .build();

        /*
         * * ENTERPRISE SECURITY NOTE (Handling the Password):
         * Because we strictly omitted the password from the UserModel to prevent accidental exposure,
         * this handler is the exact place where you must secure the password before persistence.
         * * In a complete production system, you would inject a PasswordEncoder (e.g., BCrypt) into this class:
         * String hashedPassword = passwordEncoder.encode(input.getRawPassword());
         * * Then, you would pass BOTH the user model and the hashed password to your infrastructure port:
         * return userPort.save(newUser, hashedPassword);
         */

        // The handler remains completely agnostic to whether this saves to H2, PostgreSQL, or MongoDB.
        UserModel savedUser = userPort.save(newUser);
        publishUserCreatedEvent(savedUser);
        
        log.info("user.create.success userId={} username={}", savedUser.getId(), savedUser.getUsername());
        
        return savedUser;
    }

    private void publishUserCreatedEvent(UserModel savedUser) {
        log.info("user.created-event.publish userId={}", savedUser.getId());
        eventPublisherPort.publishUserCreated(savedUser.getId());
    }
}
