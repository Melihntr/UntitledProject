package com.project.common.infrastructure.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.common.domain.exception.InvalidTokenException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Duration;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtTokenServiceTest {

    private JwtTokenService service;

    @BeforeEach
    void setUp() {
        service = new JwtTokenService(new ObjectMapper());
        ReflectionTestUtils.setField(service, "secret", "test-secret-that-is-long-enough");
        ReflectionTestUtils.setField(service, "accessTokenTtl", Duration.ofMinutes(15));
        ReflectionTestUtils.setField(service, "refreshTokenTtl", Duration.ofHours(8));
    }

    @Test
    void createAccessTokenAndParseClaims() {
        String token = service.createAccessToken("user-1", AuthConstants.ROLE_USER);

        TokenClaims claims = service.parseAndValidate(token, AuthConstants.TOKEN_TYPE_ACCESS);

        assertThat(claims.tokenId()).isNotBlank();
        assertThat(claims.userId()).isEqualTo("user-1");
        assertThat(claims.role()).isEqualTo(AuthConstants.ROLE_USER);
        assertThat(claims.tokenType()).isEqualTo(AuthConstants.TOKEN_TYPE_ACCESS);
        assertThat(claims.expiresAt()).isAfter(claims.issuedAt());
        assertThat(service.accessTokenTtlSeconds()).isEqualTo(900);
    }

    @Test
    void createRefreshTokenAndParseClaims() {
        String token = service.createRefreshToken("admin-1", AuthConstants.ROLE_ADMIN);

        TokenClaims claims = service.parseAndValidate(token, AuthConstants.TOKEN_TYPE_REFRESH);

        assertThat(claims.userId()).isEqualTo("admin-1");
        assertThat(claims.role()).isEqualTo(AuthConstants.ROLE_ADMIN);
    }

    @Test
    void parseRejectsMissingToken() {
        assertThatThrownBy(() -> service.parseAndValidate(" ", AuthConstants.TOKEN_TYPE_ACCESS))
                .isInstanceOf(InvalidTokenException.class)
                .hasMessage("Token is missing.");
    }

    @Test
    void parseRejectsInvalidFormat() {
        assertThatThrownBy(() -> service.parseAndValidate("abc.def", AuthConstants.TOKEN_TYPE_ACCESS))
                .isInstanceOf(InvalidTokenException.class)
                .hasMessage("Token format is invalid.");
    }

    @Test
    void parseRejectsInvalidSignature() {
        String token = service.createAccessToken("user-1", AuthConstants.ROLE_USER);

        assertThatThrownBy(() -> service.parseAndValidate(token + "x", AuthConstants.TOKEN_TYPE_ACCESS))
                .isInstanceOf(InvalidTokenException.class)
                .hasMessage("Token signature is invalid.");
    }

    @Test
    void parseRejectsInvalidPayload() {
        String token = "eyJhbGciOiJIUzI1NiJ9." + Base64.getUrlEncoder().withoutPadding().encodeToString("bad".getBytes()) + ".x";

        assertThatThrownBy(() -> service.parseAndValidate(token, AuthConstants.TOKEN_TYPE_ACCESS))
                .isInstanceOf(InvalidTokenException.class);
    }

    @Test
    void parseRejectsWrongTokenType() {
        String token = service.createRefreshToken("user-1", AuthConstants.ROLE_USER);

        assertThatThrownBy(() -> service.parseAndValidate(token, AuthConstants.TOKEN_TYPE_ACCESS))
                .isInstanceOf(InvalidTokenException.class)
                .hasMessage("Token type is invalid.");
    }

    @Test
    void parseRejectsExpiredToken() {
        ReflectionTestUtils.setField(service, "accessTokenTtl", Duration.ofSeconds(-1));
        String token = service.createAccessToken("user-1", AuthConstants.ROLE_USER);

        assertThatThrownBy(() -> service.parseAndValidate(token, AuthConstants.TOKEN_TYPE_ACCESS))
                .isInstanceOf(InvalidTokenException.class)
                .hasMessage("Token has expired.");
    }

    @Test
    void privateClaimMappingRejectsMissingStringAndNumberClaims() {
        Map<String, Object> missingString = validPayload();
        missingString.put("sub", " ");
        assertThatThrownBy(() -> ReflectionTestUtils.invokeMethod(service, "toClaims", missingString))
                .isInstanceOf(InvalidTokenException.class)
                .hasMessage("Token claim is missing: sub");

        Map<String, Object> nonString = validPayload();
        nonString.put("sub", 42);
        assertThatThrownBy(() -> ReflectionTestUtils.invokeMethod(service, "toClaims", nonString))
                .isInstanceOf(InvalidTokenException.class)
                .hasMessage("Token claim is missing: sub");

        Map<String, Object> missingNumber = validPayload();
        missingNumber.put("iat", "now");
        assertThatThrownBy(() -> ReflectionTestUtils.invokeMethod(service, "toClaims", missingNumber))
                .isInstanceOf(InvalidTokenException.class)
                .hasMessage("Token claim is missing: iat");
    }

    @Test
    void privateDecodeAndSignatureErrorBranchesReturnInvalidTokenException() {
        assertThatThrownBy(() -> ReflectionTestUtils.invokeMethod(service, "decodeJson", "%%%"))
                .isInstanceOf(InvalidTokenException.class)
                .hasMessage("Token payload is invalid.");

        ReflectionTestUtils.setField(service, "secret", null);
        assertThatThrownBy(() -> ReflectionTestUtils.invokeMethod(service, "sign", "value"))
                .isInstanceOf(InvalidTokenException.class)
                .hasMessage("Token signature could not be created.");
    }

    @Test
    void privateEncodeErrorBranchReturnsInvalidTokenException() {
        JwtTokenService broken = new JwtTokenService(new ObjectMapper() {
            @Override
            public byte[] writeValueAsBytes(Object value) {
                throw new IllegalStateException("boom");
            }
        });

        assertThatThrownBy(() -> ReflectionTestUtils.invokeMethod(broken, "encodeJson", Map.of("a", "b")))
                .isInstanceOf(InvalidTokenException.class)
                .hasMessage("Token could not be created.");
    }

    private Map<String, Object> validPayload() {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("jti", "token-1");
        payload.put("sub", "user-1");
        payload.put("role", "USER");
        payload.put("type", "ACCESS");
        payload.put("iat", 1L);
        payload.put("exp", 2L);
        return payload;
    }
}
