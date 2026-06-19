package com.project.notification.infrastructure.api.dto;

import com.project.notification.domain.model.NotificationType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record NotificationRequest(
        @NotBlank(message = "Event ID cannot be blank") String eventId,
        @NotNull(message = "Notification type is required") NotificationType type,
        @NotBlank(message = "Source service cannot be blank") String sourceService,
        @NotBlank(message = "Recipient ID cannot be blank") String recipientId,
        @NotBlank(message = "Title cannot be blank") @Size(max = 255, message = "Title cannot exceed 255 characters") String title,
        @NotBlank(message = "Message cannot be blank") @Size(max = 1000, message = "Message cannot exceed 1000 characters") String message,
        @NotBlank(message = "Reference ID cannot be blank") String referenceId,
        @NotNull(message = "Amount is required") @Positive(message = "Amount must be positive") BigDecimal amount,
        @NotBlank(message = "Currency cannot be blank") @Size(min = 3, max = 3, message = "Currency must be a 3-letter code") String currency
) {
}
