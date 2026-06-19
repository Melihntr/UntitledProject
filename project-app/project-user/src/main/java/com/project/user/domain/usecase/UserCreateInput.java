package com.project.user.domain.usecase;

import com.project.user.domain.validation.ValidUsername;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Core domain input model representing a request to register a new user.
 * This model encapsulates the exact data required by the business use case.
 */
@Getter
@Setter // Restored to allow MapStruct and Jackson to perform data binding
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserCreateInput {

    /**
     * The desired username.
     * Validated against custom domain rules (e.g., no profanity, specific regex constraints) 
     * utilizing the custom @ValidUsername annotation.
     */
    @ValidUsername
    private String username;

    /**
     * The user's primary email address.
     * Must conform to standard email formatting to ensure the system can deliver 
     * verification links and notifications.
     */
    @Email(message = "Business Rule Violation: Email format is strictly invalid")
    @NotBlank(message = "Business Rule Violation: Email is a mandatory field")
    private String email;

    /**
     * The plaintext password provided by the user.
     * * Security Note: It is intentionally named 'rawPassword' to constantly remind 
     * domain developers that this string is unhashed and MUST be encrypted 
     * (e.g., via PasswordEncoder) before being transformed into the final UserRecord/Entity.
     */
    @NotBlank(message = "Business Rule Violation: Password cannot be blank")
    private String rawPassword;

}
