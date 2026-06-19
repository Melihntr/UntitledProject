package com.project.common.infrastructure.tracing;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;

import static org.assertj.core.api.Assertions.assertThat;

class TraceIdProviderTest {

    private final TraceIdProvider traceIdProvider = new TraceIdProvider();

    @AfterEach
    void cleanup() {
        MDC.clear();
    }

    @Test
    void currentTraceIdOrNew_returnsExistingMdcTraceId() {
        MDC.put("traceId", "trace-1");

        assertThat(traceIdProvider.currentTraceIdOrNew()).isEqualTo("trace-1");
    }

    @Test
    void currentTraceIdOrNew_generatesTraceIdWhenMissing() {
        String traceId = traceIdProvider.currentTraceIdOrNew();

        assertThat(traceId).isNotBlank();
        assertThat(MDC.get("traceId")).isEqualTo(traceId);
    }
}
