package com.project.user.api.dto;

import lombok.Builder;
import lombok.Getter;

/**
 * A lightweight Data Transfer Object (DTO) used for outgoing API responses that require 
 * only minimal user information.
 * This is typically utilized for populating UI dropdowns, brief list views, or public 
 * summaries where exposing sensitive or bulky data (like email, password hashes, 
 * creation dates, or account status) is unnecessary and poses a security/bandwidth risk.
 */
@Getter
@Builder
public class BasicUserResponse {

    /**
     * The unique identifier of the user.
     * Used by the client as a reference key for subsequent API calls or UI state management.
     */
    private String id;

    /**
     * The public-facing username of the user.
     * Used for display purposes in the UI.
     */
    private String username;
    
    // Enterprise Note: Sensitive or large fields (e.g., email, createdAt, isActive) 
    // are intentionally omitted from this projection to enforce strict Data Minimization.
}