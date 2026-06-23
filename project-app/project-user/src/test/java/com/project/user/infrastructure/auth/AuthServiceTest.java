package com.project.user.infrastructure.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.common.domain.exception.AuthenticationFailedException;
import com.project.common.domain.exception.InvalidTokenException;
import com.project.common.infrastructure.security.AuthConstants;
import com.project.common.infrastructure.security.JwtTokenService;
import com.project.common.infrastructure.security.RefreshTokenStoreService;
import com.project.common.infrastructure.security.TokenBlacklistService;
import com.project.common.infrastructure.security.TokenClaims;
import com.project.user.domain.port.PasswordHasherPort;
import com.project.user.infrastructure.api.dto.AuthResponse;
import com.project.user.infrastructure.entity.UserEntity;
import com.project.user.infrastructure.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Duration;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AuthServiceTest {

    private UserRepository userRepository;
    private PasswordHasherPort passwordHasherPort;
    private JwtTokenService jwtTokenService;
    private RefreshTokenStoreService refreshTokenStoreService;
    private TokenBlacklistService tokenBlacklistService;
    private AuthService authService;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        passwordHasherPort = mock(PasswordHasherPort.class);
        jwtTokenService = new JwtTokenService(new ObjectMapper());
        ReflectionTestUtils.setField(jwtTokenService, "secret", "auth-service-secret-that-is-long-enough");
        ReflectionTestUtils.setField(jwtTokenService, "accessTokenTtl", Duration.ofMinutes(15));
        ReflectionTestUtils.setField(jwtTokenService, "refreshTokenTtl", Duration.ofHours(8));
        refreshTokenStoreService = new RefreshTokenStoreService();
        tokenBlacklistService = new TokenBlacklistService();
        authService = new AuthService(
                userRepository,
                passwordHasherPort,
                jwtTokenService,
                refreshTokenStoreService,
                tokenBlacklistService
        );
    }

    @Test
    void loginWithValidCredentialsIssuesTokensAndStoresRefreshToken() {
        UserEntity user = user("user-1", "alice", "hash", AuthConstants.ROLE_USER);
        when(userRepository.findByUsernameAndIsUserDeletedFalse("alice")).thenReturn(Optional.of(user));
        when(passwordHasherPort.matches("secret", "hash")).thenReturn(true);

        AuthResponse response = authService.login("alice", "secret");

        assertThat(response.getAccessToken()).isNotBlank();
        assertThat(response.getRefreshToken()).isNotBlank();
        assertThat(response.getTokenType()).isEqualTo("Bearer");
        assertThat(response.getExpiresInSeconds()).isEqualTo(900);
        assertThat(response.getUserId()).isEqualTo("user-1");
        assertThat(response.getRole()).isEqualTo(AuthConstants.ROLE_USER);
        TokenClaims refreshClaims = jwtTokenService.parseAndValidate(response.getRefreshToken(), AuthConstants.TOKEN_TYPE_REFRESH);
        assertThat(refreshTokenStoreService.validate(refreshClaims).userId()).isEqualTo("user-1");
    }

    @Test
    void loginRejectsUnknownUserAndBadPassword() {
        when(userRepository.findByUsernameAndIsUserDeletedFalse("missing")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> authService.login("missing", "secret"))
                .isInstanceOf(AuthenticationFailedException.class);

        UserEntity user = user("user-1", "alice", "hash", AuthConstants.ROLE_USER);
        when(userRepository.findByUsernameAndIsUserDeletedFalse("alice")).thenReturn(Optional.of(user));
        when(passwordHasherPort.matches("wrong", "hash")).thenReturn(false);
        assertThatThrownBy(() -> authService.login("alice", "wrong"))
                .isInstanceOf(AuthenticationFailedException.class);
    }

    @Test
    void refreshRotatesRefreshTokenAndRejectsRevokedOldToken() {
        UserEntity user = user("user-1", "alice", "hash", AuthConstants.ROLE_USER);
        when(userRepository.findByUsernameAndIsUserDeletedFalse("alice")).thenReturn(Optional.of(user));
        when(passwordHasherPort.matches("secret", "hash")).thenReturn(true);
        AuthResponse login = authService.login("alice", "secret");
        when(userRepository.findByIdAndIsUserDeletedFalse("user-1")).thenReturn(Optional.of(user));

        AuthResponse refreshed = authService.refresh(login.getRefreshToken());

        assertThat(refreshed.getRefreshToken()).isNotEqualTo(login.getRefreshToken());
        assertThatThrownBy(() -> authService.refresh(login.getRefreshToken()))
                .isInstanceOf(InvalidTokenException.class);
    }

    @Test
    void refreshRejectsInactiveUser() {
        UserEntity user = user("user-1", "alice", "hash", AuthConstants.ROLE_USER);
        String refreshToken = jwtTokenService.createRefreshToken("user-1", AuthConstants.ROLE_USER);
        TokenClaims claims = jwtTokenService.parseAndValidate(refreshToken, AuthConstants.TOKEN_TYPE_REFRESH);
        refreshTokenStoreService.store(claims);
        when(userRepository.findByIdAndIsUserDeletedFalse("user-1")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.refresh(refreshToken))
                .isInstanceOf(AuthenticationFailedException.class)
                .hasMessage("User is not active.");
    }

    @Test
    void logoutBlacklistsAccessTokenAndRevokesRefreshToken() {
        UserEntity user = user("user-1", "alice", "hash", AuthConstants.ROLE_USER);
        when(userRepository.findByUsernameAndIsUserDeletedFalse("alice")).thenReturn(Optional.of(user));
        when(passwordHasherPort.matches("secret", "hash")).thenReturn(true);
        AuthResponse login = authService.login("alice", "secret");

        authService.logout(AuthConstants.BEARER_PREFIX + login.getAccessToken(), login.getRefreshToken());

        TokenClaims accessClaims = jwtTokenService.parseAndValidate(login.getAccessToken(), AuthConstants.TOKEN_TYPE_ACCESS);
        assertThat(tokenBlacklistService.isBlacklisted(accessClaims.tokenId())).isTrue();
        TokenClaims refreshClaims = jwtTokenService.parseAndValidate(login.getRefreshToken(), AuthConstants.TOKEN_TYPE_REFRESH);
        assertThatThrownBy(() -> refreshTokenStoreService.validate(refreshClaims))
                .isInstanceOf(InvalidTokenException.class);
    }

    @Test
    void logoutAllowsMissingTokens() {
        authService.logout(null, null);
        authService.logout("Basic abc", " ");
    }

    private UserEntity user(String id, String username, String passwordHash, String role) {
        UserEntity user = new UserEntity();
        user.setId(id);
        user.setUsername(username);
        user.setPasswordHash(passwordHash);
        user.setRole(role);
        return user;
    }
}
