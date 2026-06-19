package com.project.common.security;

import com.project.common.exception.AccessDeniedException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UserHeaderInterceptorTest {

    private final UserHeaderInterceptor interceptor = new UserHeaderInterceptor();

    @AfterEach
    void cleanup() {
        MDC.clear();
    }

    @Test
    void preHandle_getRequestStoresUserInRequestContextAndMdc() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        Object handler = new Object();
        when(request.getMethod()).thenReturn("GET");
        when(request.getHeader(UserHeaderInterceptor.USER_ID_HEADER)).thenReturn("user-1");

        assertThat(interceptor.preHandle(request, response, handler)).isTrue();

        verify(request).setAttribute(UserHeaderInterceptor.USER_ID_ATTRIBUTE, "user-1");
        assertThat(MDC.get("userId")).isEqualTo("user-1");
    }

    @Test
    void preHandle_getRequestWithoutUserHeaderThrowsAccessDenied() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        when(request.getMethod()).thenReturn("GET");
        when(request.getHeader(UserHeaderInterceptor.USER_ID_HEADER)).thenReturn(" ");

        assertThatThrownBy(() -> interceptor.preHandle(request, response, new Object()))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining(UserHeaderInterceptor.USER_ID_HEADER);
    }

    @Test
    void preHandle_getRequestWithNullUserHeaderThrowsAccessDenied() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        when(request.getMethod()).thenReturn("GET");

        assertThatThrownBy(() -> interceptor.preHandle(request, response, new Object()))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining(UserHeaderInterceptor.USER_ID_HEADER);
    }

    @Test
    void preHandle_nonGetRequestDoesNotRequireUserHeader() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        when(request.getMethod()).thenReturn("POST");

        assertThat(interceptor.preHandle(request, response, new Object())).isTrue();

        verify(request, never()).getHeader(UserHeaderInterceptor.USER_ID_HEADER);
    }

    @Test
    void afterCompletionClearsOnlyUserIdFromMdc() {
        MDC.put("traceId", "trace-1");
        MDC.put("userId", "user-1");

        interceptor.afterCompletion(mock(HttpServletRequest.class), mock(HttpServletResponse.class), new Object(), null);

        assertThat(MDC.get("userId")).isNull();
        assertThat(MDC.get("traceId")).isEqualTo("trace-1");
    }
}
