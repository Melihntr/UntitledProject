package com.project.user.infrastructure.api.dto;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AuthDtoTest {

    @Test
    void loginRequestGettersAndSettersWork() {
        LoginRequest request = new LoginRequest();
        request.setUsername("alice");
        request.setPassword("secret");

        assertThat(request.getUsername()).isEqualTo("alice");
        assertThat(request.getPassword()).isEqualTo("secret");
    }

    @Test
    void refreshAndLogoutRequestGettersAndSettersWork() {
        RefreshTokenRequest refresh = new RefreshTokenRequest();
        refresh.setRefreshToken("refresh");
        LogoutRequest logout = new LogoutRequest();
        logout.setRefreshToken("logout-refresh");

        assertThat(refresh.getRefreshToken()).isEqualTo("refresh");
        assertThat(logout.getRefreshToken()).isEqualTo("logout-refresh");
    }

    @Test
    void authResponseBuilderPopulatesAllFields() {
        AuthResponse response = AuthResponse.builder()
                .accessToken("access")
                .refreshToken("refresh")
                .tokenType("Bearer")
                .expiresInSeconds(900)
                .userId("user-1")
                .role("USER")
                .build();

        assertThat(response.getAccessToken()).isEqualTo("access");
        assertThat(response.getRefreshToken()).isEqualTo("refresh");
        assertThat(response.getTokenType()).isEqualTo("Bearer");
        assertThat(response.getExpiresInSeconds()).isEqualTo(900);
        assertThat(response.getUserId()).isEqualTo("user-1");
        assertThat(response.getRole()).isEqualTo("USER");
    }
}
