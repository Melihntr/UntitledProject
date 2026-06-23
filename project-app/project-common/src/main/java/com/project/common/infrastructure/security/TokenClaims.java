package com.project.common.infrastructure.security;

import java.time.Instant;

public record TokenClaims(
        String tokenId,
        String userId,
        String role,
        String tokenType,
        Instant issuedAt,
        Instant expiresAt) {
}
