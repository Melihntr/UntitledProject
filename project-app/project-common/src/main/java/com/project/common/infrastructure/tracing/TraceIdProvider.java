package com.project.common.infrastructure.tracing;

import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.UUID;

@Component
public class TraceIdProvider {

    private static final String TRACE_ID_MDC_KEY = "traceId";

    public String currentTraceIdOrNew() {
        String traceId = MDC.get(TRACE_ID_MDC_KEY);
        if (StringUtils.hasText(traceId)) {
            return traceId;
        }

        String generatedTraceId = UUID.randomUUID().toString();
        MDC.put(TRACE_ID_MDC_KEY, generatedTraceId);
        return generatedTraceId;
    }
}
