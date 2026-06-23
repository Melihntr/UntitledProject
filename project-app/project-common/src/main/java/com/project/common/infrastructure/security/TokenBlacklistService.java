package com.project.common.infrastructure.security;

import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class TokenBlacklistService {

    private final Map<String, Instant> blacklist = new ConcurrentHashMap<>();

    public void blacklist(String tokenId, Instant expiresAt) {
        blacklist.put(tokenId, expiresAt);
    }

    public boolean isBlacklisted(String tokenId) {
        removeExpired();
        return blacklist.containsKey(tokenId);
    }

    private void removeExpired() {
        Instant now = Instant.now();
        blacklist.entrySet().removeIf(entry -> entry.getValue().isBefore(now));
    }
}
