package com.project.notification.domain.handler;

import com.project.notification.domain.model.NotificationModel;
import com.project.notification.domain.model.NotificationResult;
import com.project.notification.domain.model.NotificationSaveResult;
import com.project.notification.domain.model.NotificationStatus;
import com.project.notification.domain.model.NotificationType;
import com.project.notification.domain.usecase.RecordNotificationInput;
import com.project.notification.domain.port.NotificationPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RecordNotificationHandlerTest {

    @Mock
    private NotificationPort notificationPort;

    @InjectMocks
    private RecordNotificationHandler handler;

    @Test
    void recordNotification_savesDomainModelForNewNotification() {
        when(notificationPort.findByEventId("tx-42")).thenReturn(Optional.empty());
        when(notificationPort.save(any(NotificationModel.class)))
                .thenReturn(new NotificationSaveResult(model(), false));

        NotificationResult result = handler.recordNotification(input());

        ArgumentCaptor<NotificationModel> captor = ArgumentCaptor.forClass(NotificationModel.class);
        verify(notificationPort).save(captor.capture());
        NotificationModel newModel = captor.getValue();
        assertThat(newModel.id()).isNull();
        assertThat(newModel.eventId()).isEqualTo("tx-42");
        assertThat(newModel.type()).isEqualTo(NotificationType.TRANSFER_RECEIVED);
        assertThat(newModel.sourceService()).isEqualTo("enterprise-app");
        assertThat(newModel.recipientId()).isEqualTo("user-42");
        assertThat(newModel.title()).isEqualTo("Transfer received");
        assertThat(newModel.message()).contains("25.00");
        assertThat(newModel.referenceId()).isEqualTo("tx-42");
        assertThat(newModel.amount()).isEqualByComparingTo("25.00");
        assertThat(newModel.currency()).isEqualTo("TRY");
        assertThat(newModel.status()).isEqualTo(NotificationStatus.RECORDED);
        assertThat(newModel.createdAt()).isNull();
        assertThat(result.notificationId()).isEqualTo("notification-42");
        assertThat(result.duplicate()).isFalse();
    }

    @Test
    void recordNotification_returnsExistingNotificationForDuplicateEvent() {
        NotificationModel existing = model();
        when(notificationPort.findByEventId("tx-42")).thenReturn(Optional.of(existing));

        NotificationResult result = handler.recordNotification(input());

        assertThat(result.notificationId()).isEqualTo("notification-42");
        assertThat(result.eventId()).isEqualTo("tx-42");
        assertThat(result.status()).isEqualTo(NotificationStatus.RECORDED);
        assertThat(result.duplicate()).isTrue();
        assertThat(result.createdAt()).isEqualTo(existing.createdAt());
        verify(notificationPort, never()).save(any());
    }

    @Test
    void recordNotification_returnsConcurrentDuplicateResult() {
        when(notificationPort.findByEventId("tx-42")).thenReturn(Optional.empty());
        when(notificationPort.save(any(NotificationModel.class)))
                .thenReturn(new NotificationSaveResult(model(), true));

        NotificationResult result = handler.recordNotification(input());

        assertThat(result.notificationId()).isEqualTo("notification-42");
        assertThat(result.duplicate()).isTrue();
    }

    @Test
    void recordNotification_logsAndRethrowsFailures() {
        when(notificationPort.findByEventId("tx-42")).thenThrow(new IllegalStateException("boom"));

        assertThatThrownBy(() -> handler.recordNotification(input()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("boom");
    }

    private RecordNotificationInput input() {
        return new RecordNotificationInput(
                "tx-42", NotificationType.TRANSFER_RECEIVED, "enterprise-app", "user-42",
                "Transfer received", "You received 25.00 TRY.", "tx-42", new BigDecimal("25.00"), "TRY");
    }

    private NotificationModel model() {
        return new NotificationModel(
                "notification-42", "tx-42", NotificationType.TRANSFER_RECEIVED, "enterprise-app", "user-42",
                "Transfer received", "You received 25.00 TRY.", "tx-42", new BigDecimal("25.00"), "TRY",
                NotificationStatus.RECORDED, LocalDateTime.of(2026, 6, 11, 12, 0));
    }
}
