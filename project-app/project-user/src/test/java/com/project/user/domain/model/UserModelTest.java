package com.project.user.domain.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class UserModelTest {

    @Test
    void builder_and_getters_work() {
        LocalDateTime now = LocalDateTime.now();

        UserModel model = UserModel.builder()
                .id("user-1")
                .username("alice")
                .email("alice@example.com")
                .isActive(true)
                .createdAt(now)
                .build();

        assertThat(model).isNotNull();
        assertThat(model.getId()).isEqualTo("user-1");
        assertThat(model.getUsername()).isEqualTo("alice");
        assertThat(model.getEmail()).isEqualTo("alice@example.com");
        // Lombok boolean getter for a field named 'isActive' is `isActive()`
        assertThat(model.isActive()).isTrue();
        assertThat(model.getCreatedAt()).isEqualTo(now);
    }

    @Test
    void activateUser_setsIsActiveTrue_and_deactivateUser_setsIsActiveFalse() {
        UserModel model = UserModel.builder()
                .id("user-2")
                .username("bob")
                .email("bob@example.com")
                // start inactive
                .isActive(false)
                .build();

        assertThat(model.isActive()).isFalse();

        model.activateUser();
        assertThat(model.isActive()).isTrue();

        model.deactivateUser();
        assertThat(model.isActive()).isFalse();
    }
}