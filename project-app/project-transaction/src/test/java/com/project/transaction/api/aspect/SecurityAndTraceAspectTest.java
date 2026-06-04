package com.project.transaction.api.aspect;

import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.slf4j.MDC;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

/**
 * Unit tests for SecurityAndTraceAspect.
 * These tests manipulate RequestContextHolder to provide a mocked ServletRequestAttributes
 * and verify behavior in the aspect for various header combinations.
 */
@ExtendWith(MockitoExtension.class)
class SecurityAndTraceAspectTest {

    @InjectMocks
    private SecurityAndTraceAspect aspect;

    @Mock
    private ProceedingJoinPoint joinPoint;

    @AfterEach
    void cleanup() {
        // Ensure no leftover attributes between tests
        RequestContextHolder.resetRequestAttributes();
        MDC.clear();
    }

    @Test
    void whenNoRequestAttributes_proceedsDirectly() throws Throwable {
        // Ensure there are no request attributes
        RequestContextHolder.resetRequestAttributes();

        when(joinPoint.proceed()).thenReturn("ok");

        Object result = aspect.handleSecurityAndTrace(joinPoint);

        verify(joinPoint, times(1)).proceed();
        assertThat(result).isEqualTo("ok");
    }

    @Test
    void whenMissingUserId_throwsSecurityException_andDoesNotProceed() throws Throwable {
        ServletRequestAttributes attrs = mock(ServletRequestAttributes.class);
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(attrs.getRequest()).thenReturn(request);
        RequestContextHolder.setRequestAttributes(attrs);

        when(request.getHeader("X-User-Id")).thenReturn(null);

        // Proceed should not be invoked and SecurityException expected
        assertThatThrownBy(() -> aspect.handleSecurityAndTrace(joinPoint))
                .isInstanceOf(SecurityException.class)
                .hasMessageContaining("Missing mandatory security header");

        verify(joinPoint, never()).proceed();
    }

    @Test
    void whenUserIdAndTraceProvided_setsMdcDuringProceed_andClearsAfter() throws Throwable {
        ServletRequestAttributes attrs = mock(ServletRequestAttributes.class);
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(attrs.getRequest()).thenReturn(request);
        RequestContextHolder.setRequestAttributes(attrs);

        when(request.getHeader("X-User-Id")).thenReturn("user-123");
        when(request.getHeader("X-Trace-Id")).thenReturn("trace-abc");
        when(request.getRequestURI()).thenReturn("/api/test");

        // During proceed we assert MDC contains expected values
        when(joinPoint.proceed()).thenAnswer(invocation -> {
            assertThat(MDC.get("userId")).isEqualTo("user-123");
            assertThat(MDC.get("traceId")).isEqualTo("trace-abc");
            return "result";
        });

        Object result = aspect.handleSecurityAndTrace(joinPoint);

        assertThat(result).isEqualTo("result");
        // After execution MDC should be cleared
        assertThat(MDC.get("userId")).isNull();
        assertThat(MDC.get("traceId")).isNull();

        verify(joinPoint, times(1)).proceed();
    }

    @Test
    void whenUserIdPresentAndTraceMissing_generatesTraceId_setsMdcAndClearsAfter() throws Throwable {
        ServletRequestAttributes attrs = mock(ServletRequestAttributes.class);
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(attrs.getRequest()).thenReturn(request);
        RequestContextHolder.setRequestAttributes(attrs);

        when(request.getHeader("X-User-Id")).thenReturn("user-999");
        when(request.getHeader("X-Trace-Id")).thenReturn(null);
        when(request.getRequestURI()).thenReturn("/api/other");

        // During proceed we assert MDC has a non-blank generated traceId and correct userId
        when(joinPoint.proceed()).thenAnswer(invocation -> {
            assertThat(MDC.get("userId")).isEqualTo("user-999");
            String traceId = MDC.get("traceId");
            assertThat(traceId).isNotBlank();
            return "ok";
        });

        Object result = aspect.handleSecurityAndTrace(joinPoint);

        assertThat(result).isEqualTo("ok");
        // After execution MDC must be cleared
        assertThat(MDC.get("userId")).isNull();
        assertThat(MDC.get("traceId")).isNull();

        verify(joinPoint, times(1)).proceed();
    }
}