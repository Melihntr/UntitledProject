package com.project.user.infrastructure.repository;

import com.project.user.infrastructure.entity.UserEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan; // <-- correct import
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.TestConfiguration;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("existsByEmail returns true when a user with the email exists, false otherwise")
    void existsByEmail_behaviour() {
        UserEntity user = new UserEntity();
        user.setId("u1");
        user.setEmail("alice@example.com");
        user.setUsername("alice");
        user.setActive(true); // Lombok generates setActive(...) for field 'isActive'
        user.setCreatedAt(LocalDateTime.now());

        userRepository.save(user);

        assertThat(userRepository.existsByEmail("alice@example.com")).isTrue();
        assertThat(userRepository.existsByEmail("bob@example.com")).isFalse();
    }

    @Test
    @DisplayName("existsByUsername returns true when a user with the username exists, false otherwise")
    void existsByUsername_behaviour() {
        UserEntity user = new UserEntity();
        user.setId("u2");
        user.setEmail("charlie@example.com");
        user.setUsername("charlie");
        user.setActive(true);
        user.setCreatedAt(LocalDateTime.now());

        userRepository.save(user);

        assertThat(userRepository.existsByUsername("charlie")).isTrue();
        assertThat(userRepository.existsByUsername("nobody")).isFalse();
    }

    @TestConfiguration
    @EnableAutoConfiguration
    @EntityScan(basePackageClasses = UserEntity.class)
    static class TestConfig {
        // empty - auto-configuration + entity scan is sufficient for @DataJpaTest slice
    }
}