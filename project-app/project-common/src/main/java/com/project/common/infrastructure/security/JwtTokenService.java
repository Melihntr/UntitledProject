package com.project.common.infrastructure.security;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.common.domain.exception.InvalidTokenException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class JwtTokenService {

    private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<>() {
    };

    private final ObjectMapper objectMapper;

    @Value("${app.security.jwt.secret:enterprise-banking-default-secret-that-is-long-enough}")
    private String secret;

    @Value("${app.security.jwt.access-token-ttl:PT15M}")
    private Duration accessTokenTtl;

    @Value("${app.security.jwt.refresh-token-ttl:PT8H}")
    private Duration refreshTokenTtl;

    public String createAccessToken(String userId, String role) {
        return createToken(userId, role, AuthConstants.TOKEN_TYPE_ACCESS, accessTokenTtl);
    }

    public String createRefreshToken(String userId, String role) {
        return createToken(userId, role, AuthConstants.TOKEN_TYPE_REFRESH, refreshTokenTtl);
    }

    public TokenClaims parseAndValidate(String token, String expectedTokenType) {
        if (!StringUtils.hasText(token)) {
            throw new InvalidTokenException("Token is missing.");
        }

        String[] parts = token.split("\\.");
        if (parts.length != 3) {
            throw new InvalidTokenException("Token format is invalid.");
        }

        String signedContent = parts[0] + "." + parts[1];
        if (!constantTimeEquals(parts[2], sign(signedContent))) {
            throw new InvalidTokenException("Token signature is invalid.");
        }

        Map<String, Object> payload = decodeJson(parts[1]);
        TokenClaims claims = toClaims(payload);
        if (!expectedTokenType.equals(claims.tokenType())) {
            throw new InvalidTokenException("Token type is invalid.");
        }
        if (claims.expiresAt().isBefore(Instant.now())) {
            throw new InvalidTokenException("Token has expired.");
        }
        return claims;
    }

    public long accessTokenTtlSeconds() {
        return accessTokenTtl.toSeconds();
    }

    private String createToken(String userId, String role, String tokenType, Duration ttl) {
        Instant issuedAt = Instant.now();
        Instant expiresAt = issuedAt.plus(ttl);

        Map<String, Object> header = new LinkedHashMap<>();
        header.put("alg", "HS256");
        header.put("typ", "JWT");

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("jti", UUID.randomUUID().toString());
        payload.put("sub", userId);
        payload.put("role", role);
        payload.put("type", tokenType);
        payload.put("iat", issuedAt.getEpochSecond());
        payload.put("exp", expiresAt.getEpochSecond());

        String signedContent = encodeJson(header) + "." + encodeJson(payload);
        return signedContent + "." + sign(signedContent);
    }

    private TokenClaims toClaims(Map<String, Object> payload) {
        return new TokenClaims(
                stringClaim(payload, "jti"),
                stringClaim(payload, "sub"),
                stringClaim(payload, "role"),
                stringClaim(payload, "type"),
                Instant.ofEpochSecond(longClaim(payload, "iat")),
                Instant.ofEpochSecond(longClaim(payload, "exp"))
        );
    }

    private String stringClaim(Map<String, Object> payload, String name) {
        Object value = payload.get(name);
        if (value instanceof String text && StringUtils.hasText(text)) {
            return text;
        }
        throw new InvalidTokenException("Token claim is missing: " + name);
    }

    private long longClaim(Map<String, Object> payload, String name) {
        Object value = payload.get(name);
        if (value instanceof Number number) {
            return number.longValue();
        }
        throw new InvalidTokenException("Token claim is missing: " + name);
    }

    private String encodeJson(Map<String, Object> value) {
        try {
            return base64Url(objectMapper.writeValueAsBytes(value));
        } catch (Exception exception) {
            throw new InvalidTokenException("Token could not be created.");
        }
    }

    private Map<String, Object> decodeJson(String value) {
        try {
            return objectMapper.readValue(Base64.getUrlDecoder().decode(value), MAP_TYPE);
        } catch (Exception exception) {
            throw new InvalidTokenException("Token payload is invalid.");
        }
    }

    private String sign(String value) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            return base64Url(mac.doFinal(value.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception exception) {
            throw new InvalidTokenException("Token signature could not be created.");
        }
    }

    private boolean constantTimeEquals(String first, String second) {
        return MessageDigestSafeEquals.equals(first, second);
    }

    private String base64Url(byte[] bytes) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
