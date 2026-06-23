package com.project.common.infrastructure.audit;

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
        MDC.put("traceId", "trace-update");

        entity.assignUpdateAudit();

        assertThat(entity.getUpdatedAt()).isAfter(LocalDateTime.MIN);
        assertThat(entity.getUpdatedTraceId()).isEqualTo("trace-update");
    }

    @Test
    void inheritedAndAuditSettersWork() {
        LocalDateTime createdAt = LocalDateTime.of(2026, 1, 1, 10, 0);
        LocalDateTime updatedAt = LocalDateTime.of(2026, 1, 2, 10, 0);

        entity.setId("entity-1");
        entity.setCreatedAt(createdAt);
        entity.setUpdatedAt(updatedAt);
        entity.setCreatedTraceId("trace-created");
        entity.setUpdatedTraceId("trace-updated");

        assertThat(entity.getId()).isEqualTo("entity-1");
        assertThat(entity.getCreatedAt()).isEqualTo(createdAt);
        assertThat(entity.getUpdatedAt()).isEqualTo(updatedAt);
        assertThat(entity.getCreatedTraceId()).isEqualTo("trace-created");
        assertThat(entity.getUpdatedTraceId()).isEqualTo("trace-updated");
    }

    private static final class TestAuditableEntity extends AuditableEntity {
    }
}
