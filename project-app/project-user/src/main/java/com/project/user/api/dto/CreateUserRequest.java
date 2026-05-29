package com.project.user.api.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * Data Transfer Object for incoming user creation requests.
 */
@Getter
@Setter
public class CreateUserRequest {
    private String username;
    private String email;
    private String password;
}