package com.project.user.infrastructure.api.controller;

import com.project.common.infrastructure.model.GenericResponse;
import com.project.user.infrastructure.api.dto.AuthResponse;
import com.project.user.infrastructure.api.dto.LoginRequest;
import com.project.user.infrastructure.api.dto.LogoutRequest;
import com.project.user.infrastructure.api.dto.RefreshTokenRequest;
import com.project.user.infrastructure.auth.AuthService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController controller;

    @Test
    void loginDelegatesAndWrapsResponse() {
        LoginRequest request = new LoginRequest();
        request.setUsername("alice");
        request.setPassword("secret");
        AuthResponse authResponse = AuthResponse.builder().userId("user-1").build();
        when(authService.login("alice", "secret")).thenReturn(authResponse);

        ResponseEntity<GenericResponse<AuthResponse>> response = controller.login(request);

        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getData()).isSameAs(authResponse);
        verify(authService).login("alice", "secret");
    }

    @Test
    void refreshDelegatesAndWrapsResponse() {
        RefreshTokenRequest request = new RefreshTokenRequest();
        request.setRefreshToken("refresh");
        AuthResponse authResponse = AuthResponse.builder().userId("user-1").build();
        when(authService.refresh("refresh")).thenReturn(authResponse);

        ResponseEntity<GenericResponse<AuthResponse>> response = controller.refresh(request);

        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getData()).isSameAs(authResponse);
        verify(authService).refresh("refresh");
    }

    @Test
    void logoutDelegatesAndWrapsResponse() {
        LogoutRequest request = new LogoutRequest();
        request.setRefreshToken("refresh");

        ResponseEntity<GenericResponse<Void>> response = controller.logout("Bearer access", request);

        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isTrue();
        verify(authService).logout("Bearer access", "refresh");
    }

    @Test
    void logoutAllowsMissingBody() {
        ResponseEntity<GenericResponse<Void>> response = controller.logout(HttpHeaders.AUTHORIZATION, null);

        assertThat(response.getBody()).isNotNull();
        verify(authService).logout(HttpHeaders.AUTHORIZATION, null);
    }
}
