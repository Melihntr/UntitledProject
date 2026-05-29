package com.project.user.api.dto;

import lombok.Builder;
import lombok.Getter;

/**
 * Data Transfer Object for outgoing user creation responses.
 */
@Getter
@Builder
public class CreateUserResponse {
    private String id;
    private String username;
    private String email;
    private String statusMessage;
}