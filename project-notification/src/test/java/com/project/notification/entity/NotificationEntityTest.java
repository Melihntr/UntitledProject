package com.project.notification.entity;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class NotificationEntityTest {

    @Test
    void allArgsConstructor_and_getters_work() {
        LocalDateTime createdAt = LocalDateTime.of(2026, 6, 4, 12, 0);
        NotificationEntity entity = new NotificationEntity(
                "notif-123",
                "Your transfer completed.",
                "user-42",
                createdAt
        );

        assertThat(entity).isNotNull();
        assertThat(entity.getId()).isEqualTo("notif-123");
        assertThat(entity.getMessage()).isEqualTo("Your transfer completed.");
        assertThat(entity.getRecipientId()).isEqualTo("user-42");
        assertThat(entity.getCreatedAt()).isEqualTo(createdAt);
    }

    @Test
    void noArgsConstructor_initializesIdAndCreatedAt_and_settersWork() {
        NotificationEntity entity = new NotificationEntity();

        // Field initializers should have executed even for no-args constructor
        assertThat(entity.getId()).isNotNull();
        assertThat(entity.getId()).isNotBlank();
        assertThat(entity.getCreatedAt()).isNotNull();

        // Use setters for message/recipient and verify
        entity.setMessage("Welcome!");
        entity.setRecipientId("user-99");

        assertThat(entity.getMessage()).isEqualTo("Welcome!");
        assertThat(entity.getRecipientId()).isEqualTo("user-99");
    }
}
