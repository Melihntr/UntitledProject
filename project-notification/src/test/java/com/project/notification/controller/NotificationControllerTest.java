package com.project.notification.controller;

import com.project.notification.dto.NotificationRequest;
import com.project.notification.entity.NotificationEntity;
import com.project.notification.repository.NotificationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationControllerTest {

    @Mock
    private NotificationRepository notificationRepository;

    @InjectMocks
    private NotificationController controller;

    @Captor
    private ArgumentCaptor<NotificationEntity> entityCaptor;

    @Test
    void sendNotification_savesEntity_andReturnsSuccessMap() {
        // Arrange
        NotificationRequest req = new NotificationRequest();
        req.setRecipientId("user-42");
        req.setMessage("Hello, you have a new transfer.");

        // Simulate repository returning the saved entity (optional)
        when(notificationRepository.save(org.mockito.ArgumentMatchers.any(NotificationEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        ResponseEntity<Map<String, String>> resp = controller.sendNotification(req);

        // Assert - repository save called with expected entity
        verify(notificationRepository).save(entityCaptor.capture());
        NotificationEntity saved = entityCaptor.getValue();

        assertThat(saved).isNotNull();
        assertThat(saved.getRecipientId()).isEqualTo("user-42");
        assertThat(saved.getMessage()).isEqualTo("Hello, you have a new transfer.");
        // id and createdAt are set by the entity default; they should be present
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getCreatedAt()).isNotNull();

        // Assert - response body contains status and detail mentioning the recipient
        assertThat(resp).isNotNull();
        assertThat(resp.getStatusCode().is2xxSuccessful()).isTrue();
        Map<String, String> body = resp.getBody();
        assertThat(body).isNotNull();
        assertThat(body.get("status")).isEqualTo("SUCCESS");
        assertThat(body.get("detail")).contains("user-42");
    }
}
