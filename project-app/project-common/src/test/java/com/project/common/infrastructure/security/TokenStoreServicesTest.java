package com.project.common.infrastructure.security;

import com.project.common.domain.exception.InvalidTokenException;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TokenStoreServicesTest {

    @Test
    void tokenBlacklistStoresAndExpiresTokenIds() {
        TokenBlacklistService service = new TokenBlacklistService();

        service.blacklist("token-1", Instant.now().plusSeconds(60));

        assertThat(service.isBlacklisted("token-1")).isTrue();
        service.blacklist("token-2", Instant.now().minusSeconds(1));
        assertThat(service.isBlacklisted("token-2")).isFalse();
        assertThat(service.isBlacklisted("missing")).isFalse();
    }

    @Test
    void refreshTokenStoreValidatesAndRevokesSessions() {
        RefreshTokenStoreService service = new RefreshTokenStoreService();
        TokenClaims claims = new TokenClaims(
                "refresh-1",
                "user-1",
                AuthConstants.ROLE_USER,
                AuthConstants.TOKEN_TYPE_REFRESH,
                Instant.now(),
                Instant.now().plusSeconds(60)
        );

        service.store(claims);
        RefreshTokenSession session = service.validate(claims);

        assertThat(session.tokenId()).isEqualTo("refresh-1");
        assertThat(session.revoke().revoked()).isTrue();

        service.revoke("refresh-1");
        assertThatThrownBy(() -> service.validate(claims))
                .isInstanceOf(InvalidTokenException.class)
                .hasMessage("Refresh token is not active.");
    }

    @Test
    void refreshTokenStoreRejectsMissingExpiredAndWrongOwnerSessions() {
        RefreshTokenStoreService service = new RefreshTokenStoreService();
        TokenClaims missing = new TokenClaims("missing", "user-1", "USER", "REFRESH", Instant.now(), Instant.now().plusSeconds(60));

        assertThatThrownBy(() -> service.validate(missing))
                .isInstanceOf(InvalidTokenException.class);

        TokenClaims expired = new TokenClaims("expired", "user-1", "USER", "REFRESH", Instant.now(), Instant.now().minusSeconds(1));
        service.store(expired);
        assertThatThrownBy(() -> service.validate(expired))
                .isInstanceOf(InvalidTokenException.class);

        TokenClaims stored = new TokenClaims("refresh-2", "user-1", "USER", "REFRESH", Instant.now(), Instant.now().plusSeconds(60));
        service.store(stored);
        TokenClaims wrongOwner = new TokenClaims("refresh-2", "user-2", "USER", "REFRESH", Instant.now(), Instant.now().plusSeconds(60));
        assertThatThrownBy(() -> service.validate(wrongOwner))
                .isInstanceOf(InvalidTokenException.class)
                .hasMessage("Refresh token owner is invalid.");

        service.revoke("not-stored");
    }
}
