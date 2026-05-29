package com.project.user.api.controller;

import com.project.common.event.UserCreatedEvent;
import com.project.common.model.GenericResponse;
import com.project.user.api.dto.BasicUserResponse;
import com.project.user.api.dto.CreateUserRequest;
import com.project.user.api.dto.CreateUserResponse;
import com.project.user.api.mapper.UserApiMapper;
import com.project.user.domain.model.UserCreateInput;
import com.project.user.domain.model.UserModel;
import com.project.user.domain.usecase.CreateUserHandler;
import com.project.user.domain.usecase.GetBasicUsersHandler;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for managing user-related operations.
 * Acts as the primary entry point for the User bounded context, handling incoming HTTP requests,
 * validating payloads, and orchestrating core domain use cases.
 */
@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    private final CreateUserHandler createUserHandler;
    private final GetBasicUsersHandler getBasicUsersHandler;
    private final UserApiMapper userApiMapper;
    private final ApplicationEventPublisher eventPublisher;

    // Dependency Injection via constructor
    public UserController(CreateUserHandler createUserHandler, 
                          GetBasicUsersHandler getBasicUsersHandler, 
                          UserApiMapper userApiMapper,
                          ApplicationEventPublisher eventPublisher) {
        this.createUserHandler = createUserHandler;
        this.getBasicUsersHandler = getBasicUsersHandler;
        this.userApiMapper = userApiMapper;
        this.eventPublisher = eventPublisher;
    }

    /**
     * Registers a new user in the system.
     * Upon successful persistence, an asynchronous domain event is published to notify 
     * other bounded contexts (e.g., triggering automatic Wallet creation in the Transaction module).
     *
     * @param request The validated user registration payload.
     * @return A generic response containing the newly created user's core details.
     */
    @PostMapping
    public ResponseEntity<GenericResponse<CreateUserResponse>> createUser(
            @Valid @RequestBody CreateUserRequest request) {

        logger.info("Received request to create a new user account.");

        UserCreateInput input = userApiMapper.toInput(request);
        UserModel createdUser = createUserHandler.handle(input);
        
        // Event-Driven Architecture: Publish a domain event immediately after successful creation.
        // This broadcasts the state change to the application context without tightly coupling modules.
        logger.debug("Publishing UserCreatedEvent for User ID: {}", createdUser.getId());
        eventPublisher.publishEvent(new UserCreatedEvent(createdUser.getId()));

        CreateUserResponse responseDto = userApiMapper.toResponse(createdUser);
        
        logger.info("Successfully created user account with ID: {}", createdUser.getId());
        return ResponseEntity.ok(GenericResponse.success(responseDto));
    }

    /**
     * Retrieves a lightweight list of all registered users.
     * Highly optimized for dropdown selections or administrative overviews where full 
     * profile data is unnecessary and would consume excess bandwidth.
     *
     * @return A generic response containing a list of basic user details.
     */
    @GetMapping("/basic-list")
    public ResponseEntity<GenericResponse<List<BasicUserResponse>>> getBasicUsers() {
        
        logger.info("Fetching basic user list for administrative overview.");

        // Execute the use case (passing null as this specific handler requires no filtering input)
        List<UserModel> users = getBasicUsersHandler.handle(null);
        
        // Enterprise Note: To strictly enforce the separation of concerns, this stream mapping 
        // logic is best moved into the UserApiMapper interface (e.g., userApiMapper.toBasicResponseList(users)).
        List<BasicUserResponse> responseList = users.stream()
                .map(user -> BasicUserResponse.builder()
                        .id(user.getId())
                        .username(user.getUsername())
                        .build())
                .toList();
                
        return ResponseEntity.ok(GenericResponse.success(responseList));
    }
}