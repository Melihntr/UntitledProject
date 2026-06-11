package com.project.user.api.controller;

import com.project.common.model.GenericResponse;
import com.project.user.api.dto.BasicUserResponse;
import com.project.user.api.dto.CreateUserRequest;
import com.project.user.api.dto.CreateUserResponse;
import com.project.user.api.mapper.UserApiMapper;
import com.project.user.domain.model.UserCreateInput;
import com.project.user.domain.model.UserModel;
import com.project.user.domain.usecase.CreateUserHandler;
import com.project.user.domain.usecase.DeleteUserHandler;
import com.project.user.domain.usecase.GetBasicUsersHandler;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
public class UserController {

    private final CreateUserHandler createUserHandler;
    private final GetBasicUsersHandler getBasicUsersHandler;
    private final DeleteUserHandler deleteUserHandler;
    private final UserApiMapper userApiMapper;

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

        UserCreateInput input = userApiMapper.toInput(request);
        UserModel createdUser = createUserHandler.handle(input);
        
        CreateUserResponse responseDto = userApiMapper.toResponse(createdUser);
        
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
        
        // Execute the use case (passing null as this specific handler requires no filtering input)
        List<UserModel> users = getBasicUsersHandler.handle(null);
        
        List<BasicUserResponse> responseList = userApiMapper.toBasicResponseList(users);
                
        return ResponseEntity.ok(GenericResponse.success(responseList));
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<GenericResponse<Void>> deleteUser(@PathVariable String userId) {
        deleteUserHandler.handle(userId);
        return ResponseEntity.ok(GenericResponse.success(null, "User deleted successfully."));
    }
}
