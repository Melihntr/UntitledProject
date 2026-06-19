package com.project.user.infrastructure.api.dto;

import lombok.Builder;
import lombok.Getter;

/**
 * Data Transfer Object (DTO) for outgoing user registration responses.
 * Acts as the presentation layer contract, returning the successful result 
 * of a user creation request to the API client without exposing the internal 
 * domain models or database entities.
 */
@Getter
@Builder
public class CreateUserResponse {

    /**
     * The unique, system-generated identifier for the newly created user.
     * Returned to the client so it can be cached or used for subsequent requests 
     * (e.g., fetching a profile or initiating a login).
     */
    private String id;

    /**
     * The validated and persisted public-facing username.
     */
    private String username;

    /**
     * The registered primary contact email address.
     */
    private String email;

    /**
     * A human-readable status message confirming the outcome of the operation 
     * (e.g., "User account created successfully. Please check your email for verification.").
     */
    private String statusMessage;
}