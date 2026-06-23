package com.project.common.infrastructure.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = SecurityConfigurationTest.TestApplication.class)
class SecurityConfigurationTest {

    @Autowired
    private SecurityFilterChain securityFilterChain;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void securityBeansAreCreated() {
        assertThat(securityFilterChain).isNotNull();
        assertThat(passwordEncoder.matches("secret", passwordEncoder.encode("secret"))).isTrue();
    }

    @SpringBootApplication
    static class TestApplication {
    }
}
