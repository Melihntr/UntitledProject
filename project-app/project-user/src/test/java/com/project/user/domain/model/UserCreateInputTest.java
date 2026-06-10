package com.project.user.domain.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UserCreateInputTest {

    @Test
    void builderAndAccessorsWork() {
        UserCreateInput input = UserCreateInput.builder()
                .id("u1")
                .username("alice")
                .email("alice@example.com")
                .rawPassword("secret")
                .build();

        assertThat(input.getId()).isEqualTo("u1");
        assertThat(input.getUsername()).isEqualTo("alice");
        assertThat(input.getEmail()).isEqualTo("alice@example.com");
        assertThat(input.getRawPassword()).isEqualTo("secret");
    }

    @Test
    void noArgsConstructorAndSettersWork() {
        UserCreateInput input = new UserCreateInput();
        input.setId("u2");
        input.setUsername("bob");
        input.setEmail("bob@example.com");
        input.setRawPassword("secret");

        assertThat(input.getId()).isEqualTo("u2");
        assertThat(input.getUsername()).isEqualTo("bob");
        assertThat(input.getEmail()).isEqualTo("bob@example.com");
        assertThat(input.getRawPassword()).isEqualTo("secret");
    }
}
