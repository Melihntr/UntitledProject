package com.project.user.domain.usecase;

import com.project.common.usecase.UseCaseHandler;
import com.project.user.domain.model.UserCreateInput;
import com.project.user.domain.model.UserModel;
import com.project.user.domain.port.UserPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Core business use case handler responsible for registering a new user.
 * This class orchestrates the domain logic, ensuring that the input is transformed 
 * into a valid domain model before delegating persistence to the infrastructure layer.
 */
@Service
public class CreateUserHandler implements UseCaseHandler<UserModel, UserCreateInput> {

    private static final Logger logger = LoggerFactory.getLogger(CreateUserHandler.class);

    private final UserPort userPort;

    // Dependency Injection via constructor
    public CreateUserHandler(UserPort userPort) {
        this.userPort = userPort;
    }

    /**
     * Executes the user registration business logic.
     *
     * @param input The validated domain input containing username, email, and the raw password.
     * @return The finalized and persisted user domain model.
     */
    @Override
    public UserModel handle(UserCreateInput input) {
        
        logger.info("Initiating user creation process for username: {}", input.getUsername());

        // 1. ID Generation
        // Check if an ID is explicitly provided (e.g., from a Data Seeder during startup). 
        // If not, generate a new secure random UUID for standard API requests.
        String generatedId = (input.getId() != null) ? input.getId() : UUID.randomUUID().toString();

        // 2. Map the validated Input to our Core Domain Model
        UserModel newUser = UserModel.builder()
                .id(generatedId)
                .username(input.getUsername())
                .email(input.getEmail())
                .isActive(true) // Automatically activate the user upon creation (or set to false if email verification is required)
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

        // 3. Persist the domain model via the infrastructure port
        // The handler remains completely agnostic to whether this saves to H2, PostgreSQL, or MongoDB.
        UserModel savedUser = userPort.save(newUser);
        
        logger.info("Successfully completed user creation process. Assigned ID: {}", savedUser.getId());
        
        return savedUser;
    }
}