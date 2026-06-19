package com.project.user.infrastructure.api.dto;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class UserDtoTest {

    @Test
    void createUserRequestSettersWork() {
        CreateUserRequest request = new CreateUserRequest();
        request.setUsername("alice");
        request.setEmail("alice@example.com");
        request.setPassword("password123");

        assertThat(request.getUsername()).isEqualTo("alice");
        assertThat(request.getEmail()).isEqualTo("alice@example.com");
        assertThat(request.getPassword()).isEqualTo("password123");
    }

    @Test
    void createAndBasicResponseBuildersWork() {
        CreateUserResponse createResponse = CreateUserResponse.builder()
                .id("u1")
                .username("alice")
                .email("alice@example.com")
                .statusMessage("ok")
                .build();
        BasicUserResponse basicResponse = BasicUserResponse.builder()
                .id("u1")
                .username("alice")
                .build();

        assertThat(createResponse.getId()).isEqualTo("u1");
        assertThat(createResponse.getUsername()).isEqualTo("alice");
        assertThat(createResponse.getEmail()).isEqualTo("alice@example.com");
        assertThat(createResponse.getStatusMessage()).isEqualTo("ok");
        assertThat(basicResponse.getId()).isEqualTo("u1");
        assertThat(basicResponse.getUsername()).isEqualTo("alice");
    }

    @Test
    void reportResponseDtosSupportConstructorsBuildersAndSetters() {
        LocalDateTime createdAt = LocalDateTime.of(2026, 1, 1, 10, 0);
        ActiveTransferUserResponse active = ActiveTransferUserResponse.builder()
                .username("alice")
                .amount(BigDecimal.ONE)
                .createdAt(createdAt)
                .build();
        OrphanWalletResponse orphan = new OrphanWalletResponse("w1", BigDecimal.TEN, "u1");
        UserWalletSummaryResponse summary = new UserWalletSummaryResponse();
        summary.setUsername("bob");
        summary.setEmail("bob@example.com");
        summary.setBalance(BigDecimal.ZERO);

        assertThat(active.getCreatedAt()).isEqualTo(createdAt);
        assertThat(orphan.getWalletId()).isEqualTo("w1");
        assertThat(summary.getEmail()).isEqualTo("bob@example.com");
    }

    @Test
    void reportResponseDtosExposeAllConstructorGetterAndSetterPaths() {
        LocalDateTime createdAt = LocalDateTime.of(2026, 1, 1, 10, 0);

        ActiveTransferUserResponse active = new ActiveTransferUserResponse();
        active.setUsername("alice");
        active.setAmount(BigDecimal.TEN);
        active.setCreatedAt(createdAt);
        assertThat(active.getUsername()).isEqualTo("alice");
        assertThat(active.getAmount()).isEqualByComparingTo(BigDecimal.TEN);
        assertThat(active.getCreatedAt()).isEqualTo(createdAt);
        assertThat(new ActiveTransferUserResponse("bob", BigDecimal.ONE, createdAt).getUsername()).isEqualTo("bob");

        OrphanWalletResponse orphan = new OrphanWalletResponse();
        orphan.setWalletId("wallet-1");
        orphan.setBalance(BigDecimal.ONE);
        orphan.setSupposedUserId("user-1");
        assertThat(orphan.getWalletId()).isEqualTo("wallet-1");
        assertThat(orphan.getBalance()).isEqualByComparingTo(BigDecimal.ONE);
        assertThat(orphan.getSupposedUserId()).isEqualTo("user-1");

        UserWalletSummaryResponse summary =
                new UserWalletSummaryResponse("carol", "carol@example.com", BigDecimal.TEN);
        assertThat(summary.getUsername()).isEqualTo("carol");
        assertThat(summary.getEmail()).isEqualTo("carol@example.com");
        assertThat(summary.getBalance()).isEqualByComparingTo(BigDecimal.TEN);
    }
}
