package com.project.common.security;

import com.project.common.exception.AccessDeniedException;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CurrentUserProviderTest {

    private final CurrentUserProvider provider = new CurrentUserProvider();

    @AfterEach
    void cleanup() {
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    void getUserIdReturnsRequestAttribute() {
        HttpServletRequest request = bindRequest();
        when(request.getAttribute(UserHeaderInterceptor.USER_ID_ATTRIBUTE)).thenReturn("user-1");

        assertThat(provider.getUserId()).isEqualTo("user-1");
    }

    @Test
    void getUserIdWithoutRequestThrowsAccessDenied() {
        assertThatThrownBy(provider::getUserId)
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void getUserIdWithoutValidAttributeThrowsAccessDenied() {
        HttpServletRequest request = bindRequest();
        when(request.getAttribute(UserHeaderInterceptor.USER_ID_ATTRIBUTE)).thenReturn(" ");

        assertThatThrownBy(provider::getUserId)
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void getUserIdWithoutStringAttributeThrowsAccessDenied() {
        HttpServletRequest request = bindRequest();
        when(request.getAttribute(UserHeaderInterceptor.USER_ID_ATTRIBUTE)).thenReturn(42);

        assertThatThrownBy(provider::getUserId)
                .isInstanceOf(AccessDeniedException.class);
    }

    private HttpServletRequest bindRequest() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
        return request;
    }
}
