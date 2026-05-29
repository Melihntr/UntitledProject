package com.project.notification.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Represents a persistence entity for a notification record within the independent Notification Microservice.
 * This entity is mapped to the "notifications" table in the microservice's dedicated database,
 * ensuring strict data isolation from the Core Application in alignment with microservice patterns.
 */
@Entity
@Table(name = "notifications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class NotificationEntity {

    /**
     * The unique identifier for the notification record.
     * Automatically generated as a UUID string upon instantiation.
     */
    @Id
    private String id = UUID.randomUUID().toString();

    /**
     * The actual text content of the notification to be delivered to the user.
     * This field is mandatory and cannot be null.
     */
    @Column(nullable = false)
    private String message;

    /**
     * The unique identifier of the user (recipient) who will receive this notification.
     * This links the isolated notification record to a specific user in the Core system.
     */
    @Column(nullable = false)
    private String recipientId;

    /**
     * The exact timestamp when this notification was created and recorded in the system.
     * Defaults to the current system time upon instantiation.
     */
    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}