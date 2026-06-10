package com.project.user.domain.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class UserModelTest {

    @Test
    void builderCreatesUserAndStateMethodsToggleActiveFlag() {
        LocalDateTime createdAt = LocalDateTime.of(2026, 1, 1, 10, 0);
        UserModel user = UserModel.builder()
                .id("u1")
                .username("alice")
                .email("alice@example.com")
                .isActive(false)
                .createdAt(createdAt)
                .build();

        assertThat(user.getId()).isEqualTo("u1");
        assertThat(user.getUsername()).isEqualTo("alice");
        assertThat(user.getEmail()).isEqualTo("alice@example.com");
        assertThat(user.getCreatedAt()).isEqualTo(createdAt);
        assertThat(user.isActive()).isFalse();

        user.activateUser();
        assertThat(user.isActive()).isTrue();

        user.deactivateUser();
        assertThat(user.isActive()).isFalse();
    }
}
