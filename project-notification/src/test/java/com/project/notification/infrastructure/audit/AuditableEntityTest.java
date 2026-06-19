package com.project.notification.infrastructure.audit;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class AuditableEntityTest {

    private final TestAuditableEntity entity = new TestAuditableEntity();

    @AfterEach
    void cleanup() {
        MDC.clear();
    }

    @Test
    void assignCreationAudit_setsTimestampsAndTraceId() {
        MDC.put("traceId", "trace-create");

        entity.assignCreationAudit();

        assertThat(entity.getCreatedAt()).isNotNull();
        assertThat(entity.getUpdatedAt()).isEqualTo(entity.getCreatedAt());
        assertThat(entity.getCreatedTraceId()).isEqualTo("trace-create");
        assertThat(entity.getUpdatedTraceId()).isEqualTo("trace-create");
    }

    @Test
    void assignUpdateAudit_updatesTimestampAndTraceId() {
        entity.setUpdatedAt(LocalDateTime.MIN);
        entity.setCreatedTraceId("trace-create");
        entity.setUpdatedTraceId("trace-old");
        MDC.put("traceId", "trace-update");

        entity.assignUpdateAudit();

        assertThat(entity.getUpdatedAt()).isAfter(LocalDateTime.MIN);
        assertThat(entity.getCreatedTraceId()).isEqualTo("trace-create");
        assertThat(entity.getUpdatedTraceId()).isEqualTo("trace-update");
    }

    private static final class TestAuditableEntity extends AuditableEntity {
    }
}
