package com.project.common.domain.event;

/**
 * Represents a domain event triggered when a new user is successfully created in the system.
 * This is a lightweight record that carries only the user's unique identifier, 
 * allowing other domains (such as the Transaction module for wallet creation) 
 * to react to the event asynchronously without exposing sensitive user data.
 *
 * @param userId The unique identifier of the newly created user.
 */
public record UserCreatedEvent(String userId, String traceId) {
}
