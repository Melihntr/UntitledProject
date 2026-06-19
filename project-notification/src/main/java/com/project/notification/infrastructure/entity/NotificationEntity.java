package com.project.notification.infrastructure.entity;

import com.project.notification.infrastructure.audit.AuditableEntity;
import com.project.notification.domain.model.NotificationStatus;
import com.project.notification.domain.model.NotificationType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Getter
@Setter
@NoArgsConstructor
public class NotificationEntity extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private String id;

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

    @Basic(fetch = FetchType.LAZY, optional = false)
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

    public NotificationEntity(
            String id,
            String eventId,
            NotificationType type,
            String sourceService,
            String recipientId,
            String title,
            String message,
            String referenceId,
            BigDecimal amount,
            String currency,
            NotificationStatus status,
            LocalDateTime createdAt) {
        this.id = id;
        this.eventId = eventId;
        this.type = type;
        this.sourceService = sourceService;
        this.recipientId = recipientId;
        this.title = title;
        this.message = message;
        this.referenceId = referenceId;
        this.amount = amount;
        this.currency = currency;
        this.status = status;
        setCreatedAt(createdAt);
    }

    @PrePersist
    void assignDefaultsIfMissing() {
        if (status == null) {
            status = NotificationStatus.RECORDED;
        }
    }
}
