package com.project.transaction.infrastructure.adapter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.common.tracing.TraceIdProvider;
import com.project.transaction.domain.exception.NotificationDeliveryException;
import com.project.transaction.domain.model.NotificationResult;
import com.project.transaction.infrastructure.client.NotificationHttpExchangeClient;
import com.project.transaction.infrastructure.client.NotificationResponse;
import com.project.transaction.infrastructure.client.TransferNotificationRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpHeaders;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientResponseException;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class NotificationHttpExchangeAdapterTest {

    private final NotificationHttpExchangeClient notificationClient = mock(NotificationHttpExchangeClient.class);
    private final TraceIdProvider traceIdProvider = mock(TraceIdProvider.class);
    private NotificationHttpExchangeAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new NotificationHttpExchangeAdapter(notificationClient, traceIdProvider, new ObjectMapper());
        when(traceIdProvider.currentTraceIdOrNew()).thenReturn("trace-request");
    }

    @Test
    void sendTransferReceivedNotification_returnsTypedResultAndForwardsStructuredPayload() {
        NotificationResponse clientResponse =
                new NotificationResponse("notification-1", "tx-1", "RECORDED", false, LocalDateTime.now());
        when(notificationClient.createNotification(eq("trace-request"), any())).thenReturn(clientResponse);

        NotificationResult result = adapter.sendTransferReceivedNotification("tx-1", "receiver-1", 25.0);

        ArgumentCaptor<TransferNotificationRequest> captor = ArgumentCaptor.forClass(TransferNotificationRequest.class);
        verify(notificationClient).createNotification(eq("trace-request"), captor.capture());
        TransferNotificationRequest request = captor.getValue();
        assertThat(request.eventId()).isEqualTo("tx-1");
        assertThat(request.type()).isEqualTo("TRANSFER_RECEIVED");
        assertThat(request.sourceService()).isEqualTo("enterprise-app");
        assertThat(request.recipientId()).isEqualTo("receiver-1");
        assertThat(request.title()).isEqualTo("Transfer received");
        assertThat(request.message()).contains("25.0 TRY");
        assertThat(request.referenceId()).isEqualTo("tx-1");
        assertThat(request.amount()).isEqualByComparingTo("25.0");
        assertThat(request.currency()).isEqualTo("TRY");
        assertThat(result.notificationId()).isEqualTo("notification-1");
        assertThat(result.eventId()).isEqualTo("tx-1");
        assertThat(result.status()).isEqualTo("RECORDED");
        assertThat(result.duplicate()).isFalse();
        assertThat(clientResponse.createdAt()).isNotNull();
    }

    @Test
    void sendTransferReceivedNotification_translatesTypedErrorResponse() {
        String body = """
                {"status":400,"errorCode":"NOTIFICATION_REQUEST_INVALID","message":"Invalid request","traceId":"trace-response"}
                """;
        when(notificationClient.createNotification(eq("trace-request"), any())).thenThrow(responseException(400, body));

        assertThatThrownBy(() -> adapter.sendTransferReceivedNotification("tx-1", "receiver-1", 25.0))
                .isInstanceOfSatisfying(NotificationDeliveryException.class, exception -> {
                    assertThat(exception.getHttpStatus()).isEqualTo(400);
                    assertThat(exception.getErrorCode()).isEqualTo("NOTIFICATION_REQUEST_INVALID");
                    assertThat(exception.getTraceId()).isEqualTo("trace-response");
                    assertThat(exception.getMessage()).isEqualTo("Invalid request");
                });
    }

    @Test
    void sendTransferReceivedNotification_usesRequestTraceWhenErrorResponseHasNoTrace() {
        String body = """
                {"status":503,"errorCode":"NOTIFICATION_PERSISTENCE_UNAVAILABLE","message":"Storage unavailable"}
                """;
        when(notificationClient.createNotification(eq("trace-request"), any())).thenThrow(responseException(503, body));

        assertThatThrownBy(() -> adapter.sendTransferReceivedNotification("tx-1", "receiver-1", 25.0))
                .isInstanceOfSatisfying(NotificationDeliveryException.class,
                        exception -> assertThat(exception.getTraceId()).isEqualTo("trace-request"));
    }

    @Test
    void sendTransferReceivedNotification_usesFallbackWhenErrorBodyCannotBeParsed() {
        when(notificationClient.createNotification(eq("trace-request"), any()))
                .thenThrow(responseException(500, "not-json"));

        assertThatThrownBy(() -> adapter.sendTransferReceivedNotification("tx-1", "receiver-1", 25.0))
                .isInstanceOfSatisfying(NotificationDeliveryException.class, exception -> {
                    assertThat(exception.getErrorCode()).isEqualTo("NOTIFICATION_HTTP_ERROR");
                    assertThat(exception.getMessage()).isEqualTo("Notification service returned an error.");
                    assertThat(exception.getTraceId()).isEqualTo("trace-request");
                });
    }

    @Test
    void sendTransferReceivedNotification_translatesConnectivityFailure() {
        when(notificationClient.createNotification(eq("trace-request"), any()))
                .thenThrow(new ResourceAccessException("connection refused"));

        assertThatThrownBy(() -> adapter.sendTransferReceivedNotification("tx-1", "receiver-1", 25.0))
                .isInstanceOfSatisfying(NotificationDeliveryException.class, exception -> {
                    assertThat(exception.getHttpStatus()).isEqualTo(503);
                    assertThat(exception.getErrorCode()).isEqualTo("NOTIFICATION_SERVICE_UNAVAILABLE");
                    assertThat(exception.getTraceId()).isEqualTo("trace-request");
                    assertThat(exception.getCause()).isInstanceOf(ResourceAccessException.class);
                });
    }

    private RestClientResponseException responseException(int status, String body) {
        return new RestClientResponseException(
                "notification error", status, "error", HttpHeaders.EMPTY,
                body.getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8);
    }
}
