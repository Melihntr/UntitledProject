package com.project.notification.filter;

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

    private final TraceIdFilter filter = new TraceIdFilter();

    @AfterEach
    void clearMdc() {
        MDC.clear();
    }

    @Test
    void doFilterInternal_propagatesIncomingTraceIdAndClearsMdc() throws Exception {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain chain = mock(FilterChain.class);
        when(request.getHeader(TraceIdFilter.TRACE_ID_HEADER)).thenReturn("trace-1");

        filter.doFilterInternal(request, response, chain);

        verify(response).setHeader(TraceIdFilter.TRACE_ID_HEADER, "trace-1");
        verify(chain).doFilter(request, response);
        assertThat(MDC.get(TraceIdFilter.TRACE_ID_MDC_KEY)).isNull();
    }

    @Test
    void doFilterInternal_generatesTraceIdWhenMissing() throws Exception {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain chain = mock(FilterChain.class);

        filter.doFilterInternal(request, response, chain);

        verify(response).setHeader(org.mockito.ArgumentMatchers.eq(TraceIdFilter.TRACE_ID_HEADER),
                org.mockito.ArgumentMatchers.notNull());
        verify(chain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_clearsMdcWhenRequestFails() throws Exception {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain chain = mock(FilterChain.class);
        when(request.getHeader(TraceIdFilter.TRACE_ID_HEADER)).thenReturn("trace-failure");
        org.mockito.Mockito.doThrow(new jakarta.servlet.ServletException("boom"))
                .when(chain).doFilter(request, response);

        assertThatThrownBy(() -> filter.doFilterInternal(request, response, chain))
                .isInstanceOf(jakarta.servlet.ServletException.class);

        assertThat(MDC.get(TraceIdFilter.TRACE_ID_MDC_KEY)).isNull();
    }
}
