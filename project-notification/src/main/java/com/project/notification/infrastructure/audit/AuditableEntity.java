package com.project.notification.infrastructure.audit;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.MDC;

import java.time.LocalDateTime;

@MappedSuperclass
@Getter
@Setter
public abstract class AuditableEntity {

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "created_trace_id", updatable = false)
    private String createdTraceId;

    @Column(name = "updated_trace_id")
    private String updatedTraceId;

    @PrePersist
    protected void assignCreationAudit() {
        LocalDateTime now = LocalDateTime.now();
        String traceId = MDC.get("traceId");
        createdAt = now;
        updatedAt = now;
        createdTraceId = traceId;
        updatedTraceId = traceId;
    }

    @PreUpdate
    protected void assignUpdateAudit() {
        updatedAt = LocalDateTime.now();
        updatedTraceId = MDC.get("traceId");
    }
}
