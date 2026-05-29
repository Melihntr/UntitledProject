package com.project.notification.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * Data Transfer Object representing the payload for incoming notification requests.
 * This object is used to deserialize the JSON payload received from the Core Application 
 * during inter-service communication via the REST API.
 */
@Getter
@Setter
public class NotificationRequest {

    /**
     * The unique identifier of the user who is intended to receive the notification.
     */
    private String recipientId;

    /**
     * The textual content of the notification to be delivered and stored.
     */
    private String message;
}