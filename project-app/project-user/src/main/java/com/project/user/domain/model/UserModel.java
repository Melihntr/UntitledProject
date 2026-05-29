package com.project.user.domain.model;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * The core domain model representing a User in the business context.
 * It is decoupled from the database entity and the API DTO.
 */
@Getter
@Builder
public class UserModel {

    private String id;
    private String username;
    private String email;
    private boolean isActive;
    private LocalDateTime createdAt;

    // Domain behavior/method example
    public void activateUser() {
        this.isActive = true;
    }
}