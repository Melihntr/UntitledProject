package com.project.notification.api.controller;

import com.project.notification.api.dto.NotificationRequest;
import com.project.notification.api.dto.NotificationResponse;
import com.project.notification.api.mapper.NotificationApiMapper;
import com.project.notification.domain.model.NotificationResult;
import com.project.notification.domain.model.NotificationStatus;
import com.project.notification.domain.model.NotificationType;
import com.project.notification.domain.model.RecordNotificationInput;
import com.project.notification.domain.usecase.RecordNotificationHandler;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationControllerTest {

    @Mock
    private RecordNotificationHandler recordNotificationHandler;

    @Mock
    private NotificationApiMapper notificationApiMapper;

    @InjectMocks
    private NotificationController controller;

    @Test
    void createNotification_returnsCreatedForNewNotification() {
        NotificationRequest request = request();
        NotificationResponse serviceResponse =
                new NotificationResponse("notification-1", "tx-1", NotificationStatus.RECORDED, false, LocalDateTime.now());
        RecordNotificationInput input = input();
        NotificationResult result =
                new NotificationResult("notification-1", "tx-1", NotificationStatus.RECORDED, false, serviceResponse.createdAt());
        when(notificationApiMapper.toInput(request)).thenReturn(input);
        when(recordNotificationHandler.recordNotification(input)).thenReturn(result);
        when(notificationApiMapper.toResponse(result)).thenReturn(serviceResponse);

        ResponseEntity<NotificationResponse> response = controller.createNotification(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isEqualTo(serviceResponse);
    }

    @Test
    void createNotification_returnsOkForDuplicateNotification() {
        NotificationRequest request = request();
        NotificationResponse serviceResponse =
                new NotificationResponse("notification-1", "tx-1", NotificationStatus.RECORDED, true, LocalDateTime.now());
        RecordNotificationInput input = input();
        NotificationResult result =
                new NotificationResult("notification-1", "tx-1", NotificationStatus.RECORDED, true, serviceResponse.createdAt());
        when(notificationApiMapper.toInput(request)).thenReturn(input);
        when(recordNotificationHandler.recordNotification(input)).thenReturn(result);
        when(notificationApiMapper.toResponse(result)).thenReturn(serviceResponse);

        ResponseEntity<NotificationResponse> response = controller.createNotification(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(serviceResponse);
    }

    private NotificationRequest request() {
        return new NotificationRequest(
                "tx-1", NotificationType.TRANSFER_RECEIVED, "enterprise-app", "user-1",
                "Transfer received", "You received 25 TRY.", "tx-1", BigDecimal.valueOf(25), "TRY");
    }

    private RecordNotificationInput input() {
        return new RecordNotificationInput(
                "tx-1", NotificationType.TRANSFER_RECEIVED, "enterprise-app", "user-1",
                "Transfer received", "You received 25 TRY.", "tx-1", BigDecimal.valueOf(25), "TRY");
    }
}
