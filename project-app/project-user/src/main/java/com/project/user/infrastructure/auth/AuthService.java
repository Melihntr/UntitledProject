package com.project.user.infrastructure.auth;

import com.project.common.domain.exception.AuthenticationFailedException;
import com.project.common.infrastructure.security.AuthConstants;
import com.project.common.infrastructure.security.JwtTokenService;
import com.project.common.infrastructure.security.RefreshTokenStoreService;
import com.project.common.infrastructure.security.TokenBlacklistService;
import com.project.common.infrastructure.security.TokenClaims;
import com.project.user.domain.port.PasswordHasherPort;
import com.project.user.infrastructure.api.dto.AuthResponse;
import com.project.user.infrastructure.entity.UserEntity;
import com.project.user.infrastructure.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordHasherPort passwordHasherPort;
    private final JwtTokenService jwtTokenService;
    private final RefreshTokenStoreService refreshTokenStoreService;
    private final TokenBlacklistService tokenBlacklistService;

    public AuthResponse login(String username, String password) {
        log.info("auth.login.request username={}", username);
        UserEntity user = userRepository.findByUsernameAndIsUserDeletedFalse(username)
                .orElseThrow(() -> new AuthenticationFailedException("Invalid username or password."));

        if (!passwordHasherPort.matches(password, user.getPasswordHash())) {
            throw new AuthenticationFailedException("Invalid username or password.");
        }

        AuthResponse response = issueTokens(user.getId(), user.getRole());
        log.info("auth.login.success userId={} role={}", user.getId(), user.getRole());
        return response;
    }

    public AuthResponse refresh(String refreshToken) {
        TokenClaims claims = jwtTokenService.parseAndValidate(refreshToken, AuthConstants.TOKEN_TYPE_REFRESH);
        refreshTokenStoreService.validate(claims);

        UserEntity user = userRepository.findByIdAndIsUserDeletedFalse(claims.userId())
                .orElseThrow(() -> new AuthenticationFailedException("User is not active."));

        refreshTokenStoreService.revoke(claims.tokenId());
        AuthResponse response = issueTokens(user.getId(), user.getRole());
        log.info("auth.refresh.success userId={} oldRefreshTokenId={}", user.getId(), claims.tokenId());
        return response;
    }

    public void logout(String authorizationHeader, String refreshToken) {
        String accessToken = resolveBearerToken(authorizationHeader);
        if (StringUtils.hasText(accessToken)) {
            TokenClaims accessClaims = jwtTokenService.parseAndValidate(accessToken, AuthConstants.TOKEN_TYPE_ACCESS);
            tokenBlacklistService.blacklist(accessClaims.tokenId(), accessClaims.expiresAt());
            log.info("auth.logout.access-blacklisted userId={} tokenId={}",
                    accessClaims.userId(), accessClaims.tokenId());
        }

        if (StringUtils.hasText(refreshToken)) {
            TokenClaims refreshClaims = jwtTokenService.parseAndValidate(refreshToken, AuthConstants.TOKEN_TYPE_REFRESH);
            refreshTokenStoreService.revoke(refreshClaims.tokenId());
            log.info("auth.logout.refresh-revoked userId={} tokenId={}",
                    refreshClaims.userId(), refreshClaims.tokenId());
        }
    }

    private AuthResponse issueTokens(String userId, String role) {
        String accessToken = jwtTokenService.createAccessToken(userId, role);
        String refreshToken = jwtTokenService.createRefreshToken(userId, role);
        TokenClaims refreshClaims = jwtTokenService.parseAndValidate(refreshToken, AuthConstants.TOKEN_TYPE_REFRESH);
        refreshTokenStoreService.store(refreshClaims);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresInSeconds(jwtTokenService.accessTokenTtlSeconds())
                .userId(userId)
                .role(role)
                .build();
    }

    private String resolveBearerToken(String authorizationHeader) {
        if (!StringUtils.hasText(authorizationHeader)
                || !authorizationHeader.startsWith(AuthConstants.BEARER_PREFIX)) {
            return null;
        }
        return authorizationHeader.substring(AuthConstants.BEARER_PREFIX.length());
    }
}
