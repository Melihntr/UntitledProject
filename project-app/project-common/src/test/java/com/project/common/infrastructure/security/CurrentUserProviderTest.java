package com.project.common.infrastructure.security;

import com.project.common.domain.exception.AccessDeniedException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CurrentUserProviderTest {

    private final CurrentUserProvider provider = new CurrentUserProvider();

    @AfterEach
    void cleanup() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void getUserIdReturnsAuthenticatedPrincipalUserId() {
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                new AuthenticatedUser("user-1", "USER"),
                null,
                List.of()
        ));

        assertThat(provider.getUserId()).isEqualTo("user-1");
    }

    @Test
    void getUserIdWithoutAuthenticationThrowsAccessDenied() {
        assertThatThrownBy(provider::getUserId)
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void getUserIdWithoutAuthenticatedUserPrincipalThrowsAccessDenied() {
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                "user-1",
                null,
                List.of()
        ));

        assertThatThrownBy(provider::getUserId)
                .isInstanceOf(AccessDeniedException.class);
    }
}
