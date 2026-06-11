package com.project.notification.infrastructure.adapter;

import com.project.notification.domain.model.NotificationModel;
import com.project.notification.domain.model.NotificationSaveResult;
import com.project.notification.domain.model.NotificationStatus;
import com.project.notification.domain.model.NotificationType;
import com.project.notification.infrastructure.entity.NotificationEntity;
import com.project.notification.infrastructure.mapper.NotificationInfrastructureMapper;
import com.project.notification.infrastructure.repository.NotificationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationPersistenceAdapterTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private NotificationInfrastructureMapper notificationInfrastructureMapper;

    @InjectMocks
    private NotificationPersistenceAdapter adapter;

    @Test
    void findByEventId_mapsRepositoryResult() {
        NotificationEntity entity = entity();
        NotificationModel model = model();
        when(notificationRepository.findByEventId("tx-1")).thenReturn(Optional.of(entity));
        when(notificationInfrastructureMapper.toModel(entity)).thenReturn(model);

        assertThat(adapter.findByEventId("tx-1")).contains(model);
    }

    @Test
    void findByEventId_returnsEmptyWhenMissing() {
        when(notificationRepository.findByEventId("tx-1")).thenReturn(Optional.empty());

        assertThat(adapter.findByEventId("tx-1")).isEmpty();
    }

    @Test
    void save_mapsAndPersistsNewNotification() {
        NotificationEntity entity = entity();
        NotificationModel model = model();
        when(notificationInfrastructureMapper.toEntity(model)).thenReturn(entity);
        when(notificationRepository.save(entity)).thenReturn(entity);
        when(notificationInfrastructureMapper.toModel(entity)).thenReturn(model);

        NotificationSaveResult result = adapter.save(model);

        assertThat(result.notification()).isEqualTo(model);
        assertThat(result.duplicate()).isFalse();
    }

    @Test
    void save_returnsExistingNotificationAfterConcurrentDuplicate() {
        DataIntegrityViolationException exception = new DataIntegrityViolationException("duplicate");
        NotificationEntity entity = entity();
        NotificationModel model = model();
        when(notificationInfrastructureMapper.toEntity(model)).thenReturn(entity);
        when(notificationRepository.save(entity)).thenThrow(exception);
        when(notificationRepository.findByEventId("tx-1")).thenReturn(Optional.of(entity));
        when(notificationInfrastructureMapper.toModel(entity)).thenReturn(model);

        NotificationSaveResult result = adapter.save(model);

        assertThat(result.notification()).isEqualTo(model);
        assertThat(result.duplicate()).isTrue();
    }

    @Test
    void save_rethrowsConstraintFailureWhenEventCannotBeFound() {
        DataIntegrityViolationException exception = new DataIntegrityViolationException("db down");
        NotificationEntity entity = entity();
        NotificationModel model = model();
        when(notificationInfrastructureMapper.toEntity(model)).thenReturn(entity);
        when(notificationRepository.save(entity)).thenThrow(exception);
        when(notificationRepository.findByEventId("tx-1")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> adapter.save(model)).isSameAs(exception);
    }

    private NotificationModel model() {
        return new NotificationModel(
                "notification-1", "tx-1", NotificationType.TRANSFER_RECEIVED, "enterprise-app", "user-1",
                "Transfer received", "message", "tx-1", BigDecimal.TEN, "TRY",
                NotificationStatus.RECORDED, LocalDateTime.of(2026, 6, 11, 12, 0));
    }

    private NotificationEntity entity() {
        return new NotificationEntity(
                "notification-1", "tx-1", NotificationType.TRANSFER_RECEIVED, "enterprise-app", "user-1",
                "Transfer received", "message", "tx-1", BigDecimal.TEN, "TRY",
                NotificationStatus.RECORDED, LocalDateTime.of(2026, 6, 11, 12, 0));
    }
}
