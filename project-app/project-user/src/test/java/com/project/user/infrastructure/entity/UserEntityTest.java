package com.project.user.infrastructure.entity;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class UserEntityTest {

    @Test
    void noArgsConstructor_leavesIdForPersistenceGeneration() {
        UserEntity entity = new UserEntity();

        assertThat(entity.getId()).isNull();
        assertThat(entity.isUserDeleted()).isFalse();
        assertThat(entity.getRole()).isEqualTo("USER");
    }

    @Test
    void settersAndGettersWork() {
        LocalDateTime createdAt = LocalDateTime.of(2026, 1, 1, 10, 0);
        UserEntity entity = new UserEntity();

        entity.setId("user-1");
        entity.setUsername("alice");
        entity.setEmail("alice@example.com");
        entity.setPasswordHash("hash");
        entity.setRole("ADMIN");
        entity.setUserDeleted(true);
        entity.setCreatedAt(createdAt);
        entity.setVersion(2L);

        assertThat(entity.getId()).isEqualTo("user-1");
        assertThat(entity.getUsername()).isEqualTo("alice");
        assertThat(entity.getEmail()).isEqualTo("alice@example.com");
        assertThat(entity.getPasswordHash()).isEqualTo("hash");
        assertThat(entity.getRole()).isEqualTo("ADMIN");
        assertThat(entity.isUserDeleted()).isTrue();
        assertThat(entity.getCreatedAt()).isEqualTo(createdAt);
        assertThat(entity.getVersion()).isEqualTo(2L);
    }

}
