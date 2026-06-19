package com.project.common.filter;

import com.project.common.tracing.TraceIdProvider;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TraceIdFilterTest {

    private final TraceIdProvider traceIdProvider = mock(TraceIdProvider.class);
    private final TraceIdFilter filter = new TraceIdFilter(traceIdProvider);

    @AfterEach
    void cleanup() {
        MDC.clear();
    }

    @Test
    void doFilterInternal_usesIncomingTraceIdAndClearsMdc() throws Exception {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain chain = mock(FilterChain.class);
        when(request.getHeader("X-Trace-Id")).thenReturn("trace-in");

        filter.doFilterInternal(request, response, chain);

        verify(response).setHeader("X-Trace-Id", "trace-in");
        verify(chain).doFilter(request, response);
        assertThat(MDC.get("traceId")).isNull();
    }

    @Test
    void doFilterInternal_generatesTraceIdWhenMissing() throws Exception {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain chain = mock(FilterChain.class);
        when(request.getHeader("X-Trace-Id")).thenReturn(null);
        when(traceIdProvider.currentTraceIdOrNew()).thenReturn("generated-trace");

        filter.doFilterInternal(request, response, chain);

        verify(response).setHeader("X-Trace-Id", "generated-trace");
        verify(chain).doFilter(request, response);
        assertThat(MDC.get("traceId")).isNull();
    }

    @Test
    void doFilterInternal_generatesTraceIdWhenHeaderIsEmpty() throws Exception {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain chain = mock(FilterChain.class);
        when(request.getHeader("X-Trace-Id")).thenReturn("");
        when(traceIdProvider.currentTraceIdOrNew()).thenReturn("generated-trace");

        filter.doFilterInternal(request, response, chain);

        verify(response).setHeader("X-Trace-Id", "generated-trace");
        verify(chain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_clearsMdcWhenRequestFails() throws Exception {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain chain = mock(FilterChain.class);
        when(request.getHeader("X-Trace-Id")).thenReturn("trace-failure");
        org.mockito.Mockito.doThrow(new jakarta.servlet.ServletException("boom"))
                .when(chain).doFilter(request, response);

        assertThatThrownBy(() -> filter.doFilterInternal(request, response, chain))
                .isInstanceOf(jakarta.servlet.ServletException.class);

        assertThat(MDC.get("traceId")).isNull();
    }
}
