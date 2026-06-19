package com.project.notification.infrastructure.api.dto;

import java.time.LocalDateTime;
import java.util.Map;

public record ApiErrorResponse(
        LocalDateTime timestamp,
        int status,
        String errorCode,
        String message,
        String traceId,
        String path,
        Map<String, String> validationErrors
) {
}
