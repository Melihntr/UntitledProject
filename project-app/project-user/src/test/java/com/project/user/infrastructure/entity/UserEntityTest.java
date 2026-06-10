package com.project.user.infrastructure.entity;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class UserEntityTest {

    @Test
    void noArgsConstructor_generatesId() {
        UserEntity entity = new UserEntity();

        assertThat(entity.getId()).isNotBlank();
    }

    @Test
    void settersAndGettersWork() {
        LocalDateTime createdAt = LocalDateTime.of(2026, 1, 1, 10, 0);
        UserEntity entity = new UserEntity();

        entity.setId("user-1");
        entity.setUsername("alice");
        entity.setEmail("alice@example.com");
        entity.setActive(true);
        entity.setCreatedAt(createdAt);
        entity.setVersion(2L);

        assertThat(entity.getId()).isEqualTo("user-1");
        assertThat(entity.getUsername()).isEqualTo("alice");
        assertThat(entity.getEmail()).isEqualTo("alice@example.com");
        assertThat(entity.isActive()).isTrue();
        assertThat(entity.getCreatedAt()).isEqualTo(createdAt);
        assertThat(entity.getVersion()).isEqualTo(2L);
    }

    @Test
    void assignIdIfMissing_generatesIdWhenBlank() {
        UserEntity entity = new UserEntity();
        entity.setId(" ");

        entity.assignIdIfMissing();

        assertThat(entity.getId()).isNotBlank();
    }

    @Test
    void assignIdIfMissing_generatesIdWhenNull() {
        UserEntity entity = new UserEntity();
        entity.setId(null);

        entity.assignIdIfMissing();

        assertThat(entity.getId()).isNotBlank();
    }

    @Test
    void assignIdIfMissing_keepsExistingId() {
        UserEntity entity = new UserEntity();
        entity.setId("user-1");

        entity.assignIdIfMissing();

        assertThat(entity.getId()).isEqualTo("user-1");
    }
}
