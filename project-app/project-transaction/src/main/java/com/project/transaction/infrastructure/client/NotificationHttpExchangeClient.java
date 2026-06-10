package com.project.transaction.infrastructure.client;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

@HttpExchange("/api/v1/notifications")
public interface NotificationHttpExchangeClient {

    @PostExchange(contentType = MediaType.APPLICATION_JSON_VALUE, accept = MediaType.APPLICATION_JSON_VALUE)
    NotificationResponse createNotification(
            @RequestHeader("X-Trace-Id") String traceId,
            @RequestBody TransferNotificationRequest request);
}
