package com.project.common.infrastructure.security;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

final class MessageDigestSafeEquals {

    private MessageDigestSafeEquals() {
    }

    static boolean equals(String first, String second) {
        return MessageDigest.isEqual(
                first.getBytes(StandardCharsets.UTF_8),
                second.getBytes(StandardCharsets.UTF_8)
        );
    }
}
