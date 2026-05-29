package com.project.user.domain.model;

import com.project.user.domain.validation.ValidUsername;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;

/**
 * Input model for the domain layer. 
 * This is mapped from the API's Request DTO and validated here before processing.
 */
@Getter
@Builder
public class UserCreateInput {

    @ValidUsername // Our custom annotation
    private String username;

    @Email(message = "Email format is not valid")
    @NotBlank(message = "Email is required")
    private String email;

    @NotBlank(message = "Password cannot be blank")
    private String rawPassword;

}
