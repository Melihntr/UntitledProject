package com.project.common.filter;

import com.project.common.tracing.TraceIdProvider;
import lombok.RequiredArgsConstructor;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
@RequiredArgsConstructor
public class TraceIdFilter extends OncePerRequestFilter {

    private static final String TRACE_ID_HEADER = "X-Trace-Id";
    private static final String TRACE_ID_MDC_KEY = "traceId";
    private final TraceIdProvider traceIdProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // 1. İsteğin Header'ından Trace ID'yi al. Yoksa yeni bir UUID üret.
        String traceId = request.getHeader(TRACE_ID_HEADER);
        if (traceId == null || traceId.isEmpty()) {
            traceId = traceIdProvider.currentTraceIdOrNew();
        }

        // 2. (Bu sayede @Slf4j loglarının hepsine otomatik basılabilir)
        MDC.put(TRACE_ID_MDC_KEY, traceId);

        // 3. Response Header'ına da ekle ki Client (Frontend) bu ID'yi bilsin
        response.setHeader(TRACE_ID_HEADER, traceId);

        try {
            // İsteği Controller'a ilet
            filterChain.doFilter(request, response);
        } finally {
            // İşlem bitince MDC'yi temizle (Memory leak olmaması için önemli)
            MDC.remove(TRACE_ID_MDC_KEY);
        }
    }
}
