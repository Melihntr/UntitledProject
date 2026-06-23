package com.project.common.infrastructure.security;

import com.project.common.domain.exception.InvalidTokenException;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RefreshTokenStoreService {

    private final Map<String, RefreshTokenSession> sessions = new ConcurrentHashMap<>();

    public void store(TokenClaims claims) {
        sessions.put(claims.tokenId(), new RefreshTokenSession(
                claims.tokenId(),
                claims.userId(),
                claims.role(),
                claims.expiresAt(),
                false
        ));
    }

    public RefreshTokenSession validate(TokenClaims claims) {
        removeExpired();
        RefreshTokenSession session = sessions.get(claims.tokenId());
        if (session == null || session.revoked()) {
            throw new InvalidTokenException("Refresh token is not active.");
        }
        if (!session.userId().equals(claims.userId())) {
            throw new InvalidTokenException("Refresh token owner is invalid.");
        }
        return session;
    }

    public void revoke(String tokenId) {
        RefreshTokenSession session = sessions.get(tokenId);
        if (session != null) {
            sessions.put(tokenId, session.revoke());
        }
    }

    private void removeExpired() {
        Instant now = Instant.now();
        sessions.entrySet().removeIf(entry -> entry.getValue().expiresAt().isBefore(now));
    }
}
