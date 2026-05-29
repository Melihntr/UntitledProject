package com.project.user.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * Data Transfer Object (DTO) for incoming user registration requests.
 * Acts as the presentation layer contract, capturing and strictly validating 
 * the JSON payload sent by the API client before it is allowed to enter the 
 * internal core domain layer.
 */
@Getter
@Setter
public class CreateUserRequest {

    /**
     * The desired public-facing username for the new account.
     * Enforces length constraints to prevent database bloat and ensure UI displayability.
     */
    @NotBlank(message = "Username cannot be blank")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    private String username;

    /**
     * The user's primary contact email address.
     * Validated against standard email formatting rules to ensure communication readiness.
     */
    @NotBlank(message = "Email is required")
    @Email(message = "Please provide a valid email address format")
    private String email;

    /**
     * The raw password chosen by the user.
     * * Enterprise Note: This raw value must be intercepted and securely hashed 
     * (e.g., via BCrypt or Argon2) in the domain or infrastructure layer before 
     * being persisted to the database.
     */
    @NotBlank(message = "Password cannot be blank")
    @Size(min = 8, message = "Password must be at least 8 characters long for security purposes")
    private String password;
}