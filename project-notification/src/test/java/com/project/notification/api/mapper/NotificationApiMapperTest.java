package com.project.notification.api.mapper;

import com.project.notification.api.dto.NotificationRequest;
import com.project.notification.api.dto.NotificationResponse;
import com.project.notification.domain.model.NotificationResult;
import com.project.notification.domain.model.NotificationStatus;
import com.project.notification.domain.model.NotificationType;
import com.project.notification.domain.model.RecordNotificationInput;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class NotificationApiMapperTest {

    private final NotificationApiMapper mapper = Mappers.getMapper(NotificationApiMapper.class);

    @Test
    void mapsApiRequestAndDomainResult() {
        NotificationRequest request = new NotificationRequest(
                "tx-1", NotificationType.TRANSFER_RECEIVED, "enterprise-app", "user-1",
                "Transfer received", "message", "tx-1", BigDecimal.TEN, "TRY");

        RecordNotificationInput input = mapper.toInput(request);
        LocalDateTime createdAt = LocalDateTime.of(2026, 6, 11, 12, 0);
        NotificationResponse response = mapper.toResponse(
                new NotificationResult("notification-1", "tx-1", NotificationStatus.RECORDED, true, createdAt));

        assertThat(input.eventId()).isEqualTo("tx-1");
        assertThat(input.type()).isEqualTo(NotificationType.TRANSFER_RECEIVED);
        assertThat(input.sourceService()).isEqualTo("enterprise-app");
        assertThat(input.recipientId()).isEqualTo("user-1");
        assertThat(input.title()).isEqualTo("Transfer received");
        assertThat(input.message()).isEqualTo("message");
        assertThat(input.referenceId()).isEqualTo("tx-1");
        assertThat(input.amount()).isEqualByComparingTo("10");
        assertThat(input.currency()).isEqualTo("TRY");
        assertThat(response.notificationId()).isEqualTo("notification-1");
        assertThat(response.eventId()).isEqualTo("tx-1");
        assertThat(response.status()).isEqualTo(NotificationStatus.RECORDED);
        assertThat(response.duplicate()).isTrue();
        assertThat(response.createdAt()).isEqualTo(createdAt);
    }

    @Test
    void mapsNullSourcesToNull() {
        assertThat(mapper.toInput(null)).isNull();
        assertThat(mapper.toResponse(null)).isNull();
    }
}
