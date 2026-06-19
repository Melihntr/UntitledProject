package com.project.notification.infrastructure.api.controller;

import com.project.notification.infrastructure.api.dto.NotificationRequest;
import com.project.notification.infrastructure.api.dto.NotificationResponse;
import com.project.notification.infrastructure.api.mapper.NotificationApiMapper;
import com.project.notification.domain.model.NotificationResult;
import com.project.notification.domain.handler.RecordNotificationHandler;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final RecordNotificationHandler recordNotificationHandler;
    private final NotificationApiMapper notificationApiMapper;

    @PostMapping
    public ResponseEntity<NotificationResponse> createNotification(@Valid @RequestBody NotificationRequest request) {
        NotificationResult result = recordNotificationHandler.recordNotification(notificationApiMapper.toInput(request));
        NotificationResponse response = notificationApiMapper.toResponse(result);
        HttpStatus status = response.duplicate() ? HttpStatus.OK : HttpStatus.CREATED;
        return ResponseEntity.status(status).body(response);
    }
}
