package com.project.transaction.infrastructure.client;

import java.time.LocalDateTime;
import java.util.Map;

public record NotificationErrorResponse(
        LocalDateTime timestamp,
        int status,
        String errorCode,
        String message,
        String traceId,
        String path,
        Map<String, String> validationErrors
) {
}
