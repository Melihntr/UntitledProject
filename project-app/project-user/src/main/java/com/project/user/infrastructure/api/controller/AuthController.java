package com.project.user.infrastructure.api.controller;

import com.project.common.infrastructure.model.GenericResponse;
import com.project.user.infrastructure.api.dto.AuthResponse;
import com.project.user.infrastructure.api.dto.LoginRequest;
import com.project.user.infrastructure.api.dto.LogoutRequest;
import com.project.user.infrastructure.api.dto.RefreshTokenRequest;
import com.project.user.infrastructure.auth.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<GenericResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(GenericResponse.success(
                authService.login(request.getUsername(), request.getPassword()),
                "Login completed successfully."
        ));
    }

    @PostMapping("/refresh")
    public ResponseEntity<GenericResponse<AuthResponse>> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return ResponseEntity.ok(GenericResponse.success(
                authService.refresh(request.getRefreshToken()),
                "Token refreshed successfully."
        ));
    }

    @PostMapping("/logout")
    public ResponseEntity<GenericResponse<Void>> logout(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorizationHeader,
            @RequestBody(required = false) LogoutRequest request) {
        authService.logout(authorizationHeader, request == null ? null : request.getRefreshToken());
        return ResponseEntity.ok(GenericResponse.success(null, "Logout completed successfully."));
    }
}
