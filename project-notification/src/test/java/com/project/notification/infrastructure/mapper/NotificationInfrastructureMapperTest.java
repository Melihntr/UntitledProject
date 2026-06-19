package com.project.notification.infrastructure.mapper;

import com.project.notification.domain.model.NotificationModel;
import com.project.notification.domain.model.NotificationStatus;
import com.project.notification.domain.model.NotificationType;
import com.project.notification.infrastructure.entity.NotificationEntity;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class NotificationInfrastructureMapperTest {

    private final NotificationInfrastructureMapper mapper =
            Mappers.getMapper(NotificationInfrastructureMapper.class);

    @Test
    void mapsDomainModelToNewPersistenceEntity() {
        NotificationEntity entity = mapper.toEntity(model());

        assertThat(entity.getId()).isNull();
        assertThat(entity.getCreatedAt()).isNull();
        assertThat(entity.getEventId()).isEqualTo("tx-1");
        assertThat(entity.getType()).isEqualTo(NotificationType.TRANSFER_RECEIVED);
        assertThat(entity.getSourceService()).isEqualTo("enterprise-app");
        assertThat(entity.getRecipientId()).isEqualTo("user-1");
        assertThat(entity.getTitle()).isEqualTo("Transfer received");
        assertThat(entity.getMessage()).isEqualTo("message");
        assertThat(entity.getReferenceId()).isEqualTo("tx-1");
        assertThat(entity.getAmount()).isEqualByComparingTo("10");
        assertThat(entity.getCurrency()).isEqualTo("TRY");
        assertThat(entity.getStatus()).isEqualTo(NotificationStatus.RECORDED);
    }

    @Test
    void mapsPersistenceEntityToDomainModel() {
        LocalDateTime createdAt = LocalDateTime.of(2026, 6, 11, 12, 0);
        NotificationEntity entity = new NotificationEntity(
                "notification-1", "tx-1", NotificationType.TRANSFER_RECEIVED, "enterprise-app", "user-1",
                "Transfer received", "message", "tx-1", BigDecimal.TEN, "TRY",
                NotificationStatus.RECORDED, createdAt);

        NotificationModel model = mapper.toModel(entity);

        assertThat(model.id()).isEqualTo("notification-1");
        assertThat(model.eventId()).isEqualTo("tx-1");
        assertThat(model.type()).isEqualTo(NotificationType.TRANSFER_RECEIVED);
        assertThat(model.sourceService()).isEqualTo("enterprise-app");
        assertThat(model.recipientId()).isEqualTo("user-1");
        assertThat(model.title()).isEqualTo("Transfer received");
        assertThat(model.message()).isEqualTo("message");
        assertThat(model.referenceId()).isEqualTo("tx-1");
        assertThat(model.amount()).isEqualByComparingTo("10");
        assertThat(model.currency()).isEqualTo("TRY");
        assertThat(model.status()).isEqualTo(NotificationStatus.RECORDED);
        assertThat(model.createdAt()).isEqualTo(createdAt);
    }

    @Test
    void mapsNullSourcesToNull() {
        assertThat(mapper.toEntity(null)).isNull();
        assertThat(mapper.toModel(null)).isNull();
    }

    private NotificationModel model() {
        return new NotificationModel(
                "ignored-id", "tx-1", NotificationType.TRANSFER_RECEIVED, "enterprise-app", "user-1",
                "Transfer received", "message", "tx-1", BigDecimal.TEN, "TRY",
                NotificationStatus.RECORDED, LocalDateTime.of(2026, 6, 11, 12, 0));
    }
}
