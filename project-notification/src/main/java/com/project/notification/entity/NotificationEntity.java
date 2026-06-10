package com.project.notification.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "notifications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class NotificationEntity {

    @Id
    private String id = UUID.randomUUID().toString();

    @Column(name = "event_id", nullable = false, unique = true, updatable = false)
    private String eventId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, updatable = false)
    private NotificationType type;

    @Column(name = "source_service", nullable = false, updatable = false)
    private String sourceService;

    @Column(name = "recipient_id", nullable = false, updatable = false)
    private String recipientId;

    @Column(nullable = false, updatable = false)
    private String title;

    @Column(nullable = false, length = 1000, updatable = false)
    private String message;

    @Column(name = "reference_id", nullable = false, updatable = false)
    private String referenceId;

    @Column(nullable = false, precision = 19, scale = 4, updatable = false)
    private BigDecimal amount;

    @Column(nullable = false, length = 3, updatable = false)
    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationStatus status = NotificationStatus.RECORDED;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @PrePersist
    void assignDefaultsIfMissing() {
        if (id == null || id.isBlank()) {
            id = UUID.randomUUID().toString();
        }
        if (status == null) {
            status = NotificationStatus.RECORDED;
        }
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
