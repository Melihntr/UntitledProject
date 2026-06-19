package com.project.transaction.infrastructure.adapter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.common.infrastructure.tracing.TraceIdProvider;
import com.project.transaction.domain.exception.NotificationDeliveryException;
import com.project.transaction.domain.model.NotificationResult;
import com.project.transaction.domain.port.NotificationPort;
import com.project.transaction.infrastructure.client.NotificationErrorResponse;
import com.project.transaction.infrastructure.client.NotificationHttpExchangeClient;
import com.project.transaction.infrastructure.client.NotificationResponse;
import com.project.transaction.infrastructure.client.TransferNotificationRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientResponseException;

@Component
@Slf4j
@RequiredArgsConstructor
public class NotificationHttpExchangeAdapter implements NotificationPort {

    private final NotificationHttpExchangeClient notificationClient;
    private final TraceIdProvider traceIdProvider;
    private final ObjectMapper objectMapper;

    @Override
    public NotificationResult sendTransferReceivedNotification(
            String transactionId, String recipientId, Double amount) {
        String traceId = traceIdProvider.currentTraceIdOrNew();
        TransferNotificationRequest request =
                TransferNotificationRequest.receivedTransfer(transactionId, recipientId, amount);
        try {
            log.info("notification.client.request traceId={} eventId={} type={} referenceId={} recipientId={}",
                    traceId, request.eventId(), request.type(), request.referenceId(), request.recipientId());
            NotificationResponse response = notificationClient.createNotification(traceId, request);
            log.info("notification.client.success traceId={} eventId={} notificationId={} status={} duplicate={}",
                    traceId, response.eventId(), response.notificationId(), response.status(), response.duplicate());
            return new NotificationResult(
                    response.notificationId(), response.eventId(), response.status(), response.duplicate());
        } catch (RestClientResponseException exception) {
            throw responseException(exception, traceId, request);
        } catch (ResourceAccessException exception) {
            log.error("notification.client.unavailable traceId={} eventId={} referenceId={} recipientId={}",
                    traceId, request.eventId(), request.referenceId(), request.recipientId(), exception);
            throw new NotificationDeliveryException(
                    "Notification service is unavailable.", 503, "NOTIFICATION_SERVICE_UNAVAILABLE", traceId, exception);
        }
    }

    private NotificationDeliveryException responseException(
            RestClientResponseException exception, String requestTraceId, TransferNotificationRequest request) {
        NotificationErrorResponse error = readError(exception);
        String errorCode = error == null ? "NOTIFICATION_HTTP_ERROR" : error.errorCode();
        String message = error == null ? "Notification service returned an error." : error.message();
        String responseTraceId = error == null || error.traceId() == null ? requestTraceId : error.traceId();

        log.error("notification.client.failure traceId={} eventId={} referenceId={} recipientId={} httpStatus={} errorCode={} message={}",
                responseTraceId, request.eventId(), request.referenceId(), request.recipientId(),
                exception.getStatusCode().value(), errorCode, message);
        return new NotificationDeliveryException(
                message, exception.getStatusCode().value(), errorCode, responseTraceId, exception);
    }

    private NotificationErrorResponse readError(RestClientResponseException exception) {
        try {
            return objectMapper.readValue(exception.getResponseBodyAsString(), NotificationErrorResponse.class);
        } catch (JsonProcessingException parsingException) {
            log.warn("notification.client.error-body-unreadable httpStatus={} responseBody={}",
                    exception.getStatusCode().value(), exception.getResponseBodyAsString());
            return null;
        }
    }
}
