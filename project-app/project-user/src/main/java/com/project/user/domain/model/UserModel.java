package com.project.user.domain.model;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * The core domain model representing a User within the business context.
 * This class acts as a Rich Domain Model in Clean Architecture, encapsulating 
 * both the state of the user and the core business rules/behaviors associated with it.
 * It remains strictly decoupled from persistence entities (ORMs) and API DTOs.
 */
@Getter
@Builder
public class UserModel {

    /**
     * The unique, system-generated identifier of the user.
     */
    private String id;

    /**
     * The validated, public-facing username.
     */
    private String username;

    /**
     * The user's primary contact email address.
     */
    private String email;

    /**
     * Indicates whether the user account is currently active and permitted to interact with the system.
     */
    private boolean isActive;

    /**
     * The exact timestamp when the user account was provisioned in the system.
     */
    private LocalDateTime createdAt;
    
    // Enterprise Note: Security-sensitive fields like 'hashedPassword' are often omitted 
    // from the primary profile model or placed in a dedicated UserCredentialModel. 
    // This strictly prevents accidental exposure of password hashes in application logs or DTO mappings.

    /**
     * Core business behavior to activate a user account.
     * Enforces the domain rule that transitions a user's state to active 
     * (e.g., triggered after successful email verification).
     */
    public void activateUser() {
        // Future Extension Point: Add state transition validation here 
        // (e.g., throw exception if the user is already permanently banned).
        this.isActive = true;
    }
    
    /**
     * Core business behavior to deactivate or suspend a user account.
     * Triggered by administrative fraud detection or explicit user account deletion requests.
     */
    public void deactivateUser() {
        this.isActive = false;
    }
}