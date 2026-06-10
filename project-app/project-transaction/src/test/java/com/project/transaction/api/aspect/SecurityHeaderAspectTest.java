package com.project.transaction.api.aspect;

import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.MDC;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SecurityHeaderAspectTest {

    @InjectMocks
    private SecurityHeaderAspect aspect;

    @Mock
    private ProceedingJoinPoint joinPoint;

    @AfterEach
    void cleanup() {
        RequestContextHolder.resetRequestAttributes();
        MDC.clear();
    }

    @Test
    void whenNoRequestAttributes_proceedsDirectly() throws Throwable {
        when(joinPoint.proceed()).thenReturn("ok");

        assertThat(aspect.validateUserHeader(joinPoint)).isEqualTo("ok");
        verify(joinPoint).proceed();
    }

    @Test
    void whenMissingUserId_throwsSecurityException() throws Throwable {
        HttpServletRequest request = bindRequest();
        when(request.getHeader("X-User-Id")).thenReturn(null);

        assertThatThrownBy(() -> aspect.validateUserHeader(joinPoint))
                .isInstanceOf(SecurityException.class)
                .hasMessageContaining("Missing mandatory security header");

        verify(joinPoint, never()).proceed();
    }

    @Test
    void whenBlankUserId_throwsSecurityException() throws Throwable {
        HttpServletRequest request = bindRequest();
        when(request.getHeader("X-User-Id")).thenReturn(" ");

        assertThatThrownBy(() -> aspect.validateUserHeader(joinPoint))
                .isInstanceOf(SecurityException.class)
                .hasMessageContaining("Missing mandatory security header");

        verify(joinPoint, never()).proceed();
    }

    @Test
    void whenUserIdProvided_setsUserIdDuringProceedAndRemovesOnlyUserId() throws Throwable {
        HttpServletRequest request = bindRequest();
        when(request.getHeader("X-User-Id")).thenReturn("user-1");
        when(request.getRequestURI()).thenReturn("/api/test");
        MDC.put("traceId", "trace-1");
        when(joinPoint.proceed()).thenAnswer(invocation -> {
            assertThat(MDC.get("userId")).isEqualTo("user-1");
            assertThat(MDC.get("traceId")).isEqualTo("trace-1");
            return "done";
        });

        assertThat(aspect.validateUserHeader(joinPoint)).isEqualTo("done");
        assertThat(MDC.get("userId")).isNull();
        assertThat(MDC.get("traceId")).isEqualTo("trace-1");
    }

    @Test
    void whenEndpointFails_removesUserId() throws Throwable {
        HttpServletRequest request = bindRequest();
        when(request.getHeader("X-User-Id")).thenReturn("user-1");
        when(request.getRequestURI()).thenReturn("/api/test");
        when(joinPoint.proceed()).thenThrow(new RuntimeException("boom"));

        assertThatThrownBy(() -> aspect.validateUserHeader(joinPoint))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("boom");
        assertThat(MDC.get("userId")).isNull();
    }

    private HttpServletRequest bindRequest() {
        ServletRequestAttributes attributes = mock(ServletRequestAttributes.class);
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(attributes.getRequest()).thenReturn(request);
        RequestContextHolder.setRequestAttributes(attributes);
        return request;
    }
}
