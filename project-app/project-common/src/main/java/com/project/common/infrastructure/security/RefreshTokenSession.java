package com.project.common.infrastructure.security;

import java.time.Instant;

public record RefreshTokenSession(
        String tokenId,
        String userId,
        String role,
        Instant expiresAt,
        boolean revoked) {

    public RefreshTokenSession revoke() {
        return new RefreshTokenSession(tokenId, userId, role, expiresAt, true);
    }
}
