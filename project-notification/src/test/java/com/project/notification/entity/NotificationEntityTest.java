package com.project.notification.entity;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class NotificationEntityTest {

    @Test
    void allArgsConstructor_and_getters_work() {
        LocalDateTime createdAt = LocalDateTime.of(2026, 6, 9, 12, 0);
        NotificationEntity entity = new NotificationEntity(
                "notification-1", "tx-1", NotificationType.TRANSFER_RECEIVED, "enterprise-app", "user-1",
                "Transfer received", "You received 25 TRY.", "tx-1", BigDecimal.valueOf(25), "TRY",
                NotificationStatus.RECORDED, createdAt);

        assertThat(entity.getId()).isEqualTo("notification-1");
        assertThat(entity.getEventId()).isEqualTo("tx-1");
        assertThat(entity.getType()).isEqualTo(NotificationType.TRANSFER_RECEIVED);
        assertThat(entity.getSourceService()).isEqualTo("enterprise-app");
        assertThat(entity.getRecipientId()).isEqualTo("user-1");
        assertThat(entity.getTitle()).isEqualTo("Transfer received");
        assertThat(entity.getMessage()).contains("25 TRY");
        assertThat(entity.getReferenceId()).isEqualTo("tx-1");
        assertThat(entity.getAmount()).isEqualByComparingTo("25");
        assertThat(entity.getCurrency()).isEqualTo("TRY");
        assertThat(entity.getStatus()).isEqualTo(NotificationStatus.RECORDED);
        assertThat(entity.getCreatedAt()).isEqualTo(createdAt);
    }

    @Test
    void noArgsConstructor_initializesDefaults_and_settersWork() {
        NotificationEntity entity = new NotificationEntity();
        entity.setEventId("tx-1");
        entity.setType(NotificationType.TRANSFER_RECEIVED);
        entity.setSourceService("enterprise-app");
        entity.setRecipientId("user-1");
        entity.setTitle("Transfer received");
        entity.setMessage("message");
        entity.setReferenceId("tx-1");
        entity.setAmount(BigDecimal.TEN);
        entity.setCurrency("TRY");

        assertThat(entity.getId()).isNotBlank();
        assertThat(entity.getStatus()).isEqualTo(NotificationStatus.RECORDED);
        assertThat(entity.getCreatedAt()).isNotNull();
        assertThat(entity.getEventId()).isEqualTo("tx-1");
    }

    @Test
    void assignDefaultsIfMissing_generatesAllMissingDefaults() {
        NotificationEntity entity = new NotificationEntity();
        entity.setId(null);
        entity.setStatus(null);
        entity.setCreatedAt(null);

        entity.assignDefaultsIfMissing();

        assertThat(entity.getId()).isNotBlank();
        assertThat(entity.getStatus()).isEqualTo(NotificationStatus.RECORDED);
        assertThat(entity.getCreatedAt()).isNotNull();
    }

    @Test
    void assignDefaultsIfMissing_handlesBlankIdAndKeepsExistingStatusAndTimestamp() {
        LocalDateTime createdAt = LocalDateTime.of(2026, 6, 9, 15, 0);
        NotificationEntity entity = new NotificationEntity();
        entity.setId(" ");
        entity.setStatus(NotificationStatus.RECORDED);
        entity.setCreatedAt(createdAt);

        entity.assignDefaultsIfMissing();

        assertThat(entity.getId()).isNotBlank();
        assertThat(entity.getStatus()).isEqualTo(NotificationStatus.RECORDED);
        assertThat(entity.getCreatedAt()).isEqualTo(createdAt);
    }

    @Test
    void assignDefaultsIfMissing_keepsExistingValues() {
        LocalDateTime createdAt = LocalDateTime.of(2026, 6, 9, 15, 0);
        NotificationEntity entity = new NotificationEntity();
        entity.setId("notification-1");
        entity.setStatus(NotificationStatus.RECORDED);
        entity.setCreatedAt(createdAt);

        entity.assignDefaultsIfMissing();

        assertThat(entity.getId()).isEqualTo("notification-1");
        assertThat(entity.getCreatedAt()).isEqualTo(createdAt);
    }
}
