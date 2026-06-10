package com.project.notification.service;

import com.project.notification.dto.NotificationRequest;
import com.project.notification.dto.NotificationResponse;
import com.project.notification.entity.NotificationEntity;
import com.project.notification.entity.NotificationStatus;
import com.project.notification.entity.NotificationType;
import com.project.notification.repository.NotificationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @InjectMocks
    private NotificationService notificationService;

    @Test
    void recordNotification_savesStructuredEntityFromRequest() {
        NotificationRequest request = request();
        when(notificationRepository.findByEventId("tx-42")).thenReturn(Optional.empty());
        when(notificationRepository.save(any(NotificationEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        NotificationResponse response = notificationService.recordNotification(request);

        ArgumentCaptor<NotificationEntity> captor = ArgumentCaptor.forClass(NotificationEntity.class);
        verify(notificationRepository).save(captor.capture());
        NotificationEntity entity = captor.getValue();
        assertThat(entity.getEventId()).isEqualTo("tx-42");
        assertThat(entity.getType()).isEqualTo(NotificationType.TRANSFER_RECEIVED);
        assertThat(entity.getSourceService()).isEqualTo("enterprise-app");
        assertThat(entity.getRecipientId()).isEqualTo("user-42");
        assertThat(entity.getTitle()).isEqualTo("Transfer received");
        assertThat(entity.getMessage()).contains("25.00");
        assertThat(entity.getReferenceId()).isEqualTo("tx-42");
        assertThat(entity.getAmount()).isEqualByComparingTo("25.00");
        assertThat(entity.getCurrency()).isEqualTo("TRY");
        assertThat(response.notificationId()).isEqualTo(entity.getId());
        assertThat(response.duplicate()).isFalse();
    }

    @Test
    void recordNotification_returnsExistingNotificationForDuplicateEvent() {
        NotificationEntity existing = entity();
        when(notificationRepository.findByEventId("tx-42")).thenReturn(Optional.of(existing));

        NotificationResponse response = notificationService.recordNotification(request());

        assertThat(response.notificationId()).isEqualTo("notification-42");
        assertThat(response.eventId()).isEqualTo("tx-42");
        assertThat(response.status()).isEqualTo(NotificationStatus.RECORDED);
        assertThat(response.duplicate()).isTrue();
        assertThat(response.createdAt()).isEqualTo(existing.getCreatedAt());
        verify(notificationRepository, never()).save(any());
    }

    @Test
    void recordNotification_whenRepositoryFails_rethrows() {
        when(notificationRepository.findByEventId("tx-42")).thenReturn(Optional.empty());
        when(notificationRepository.save(any(NotificationEntity.class)))
                .thenThrow(new DataIntegrityViolationException("db down"));

        assertThatThrownBy(() -> notificationService.recordNotification(request()))
                .isInstanceOf(DataIntegrityViolationException.class)
                .hasMessageContaining("db down");
    }

    @Test
    void recordNotification_treatsConcurrentUniqueConstraintRaceAsDuplicate() {
        NotificationEntity existing = entity();
        when(notificationRepository.findByEventId("tx-42"))
                .thenReturn(Optional.empty(), Optional.of(existing));
        when(notificationRepository.save(any(NotificationEntity.class)))
                .thenThrow(new DataIntegrityViolationException("duplicate event"));

        NotificationResponse response = notificationService.recordNotification(request());

        assertThat(response.notificationId()).isEqualTo("notification-42");
        assertThat(response.duplicate()).isTrue();
    }

    @Test
    void recordNotification_whenUnexpectedRepositoryFailureOccurs_rethrows() {
        when(notificationRepository.findByEventId("tx-42")).thenReturn(Optional.empty());
        when(notificationRepository.save(any(NotificationEntity.class))).thenThrow(new IllegalStateException("boom"));

        assertThatThrownBy(() -> notificationService.recordNotification(request()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("boom");
    }

    private NotificationRequest request() {
        return new NotificationRequest(
                "tx-42", NotificationType.TRANSFER_RECEIVED, "enterprise-app", "user-42",
                "Transfer received", "You received 25.00 TRY.", "tx-42", new BigDecimal("25.00"), "TRY");
    }

    private NotificationEntity entity() {
        NotificationEntity entity = new NotificationEntity();
        entity.setId("notification-42");
        entity.setEventId("tx-42");
        entity.setType(NotificationType.TRANSFER_RECEIVED);
        entity.setSourceService("enterprise-app");
        entity.setRecipientId("user-42");
        entity.setTitle("Transfer received");
        entity.setMessage("You received 25.00 TRY.");
        entity.setReferenceId("tx-42");
        entity.setAmount(new BigDecimal("25.00"));
        entity.setCurrency("TRY");
        return entity;
    }
}
