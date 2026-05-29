package com.project.notification.controller;

import com.project.notification.dto.NotificationRequest;
import com.project.notification.entity.NotificationEntity;
import com.project.notification.repository.NotificationRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * REST Controller responsible for handling inter-service communication.
 * This endpoint acts as the entry point for the Notification Microservice,
 * receiving event payloads from the Core Application and persisting them independently.
 */
@RestController
@RequestMapping("/api/v1/notifications")
public class NotificationController {

    private final NotificationRepository notificationRepository;

    // Dependency Injection via constructor
    public NotificationController(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    /**
     * Endpoint to process and persist a new notification triggered by an external microservice.
     *
     * @param request The data transfer object containing the recipient ID and the notification message.
     * @return A REST response containing a status map indicating successful persistence.
     */
    @PostMapping("/send")
    public ResponseEntity<Map<String, String>> sendNotification(@RequestBody NotificationRequest request) {
        
        NotificationEntity notification = new NotificationEntity();
        notification.setRecipientId(request.getRecipientId());
        notification.setMessage(request.getMessage());
        
        // Persist the notification record to the independent microservice database
        notificationRepository.save(notification);

        // Return a standardized JSON response map
        return ResponseEntity.ok(Map.of(
                "status", "SUCCESS",
                "detail", "Notification successfully recorded for user: " + request.getRecipientId()
        ));
    }
}