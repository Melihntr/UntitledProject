package com.project.user.infrastructure.adapter;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;

class PasswordHasherAdapterTest {

    @Test
    void hashAndMatchesDelegateToPasswordEncoder() {
        PasswordHasherAdapter adapter = new PasswordHasherAdapter(new BCryptPasswordEncoder());

        String hash = adapter.hash("secret");

        assertThat(hash).isNotEqualTo("secret");
        assertThat(adapter.matches("secret", hash)).isTrue();
        assertThat(adapter.matches("wrong", hash)).isFalse();
    }
}
