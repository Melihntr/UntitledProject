package com.project.transaction.infrastructure.repository;

import com.project.transaction.infrastructure.entity.WalletEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.annotation.Rollback;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class WalletRepositoryTest {

    @Autowired
    private WalletRepository walletRepository;

    @Test
    @Rollback
    void saveAndFindByUserId_returnsPersistedEntity() {
        // Arrange
        WalletEntity entity = new WalletEntity();
        entity.setId("w-100");
        entity.setUserId("user-100");
        entity.setBalance(250.0);
        entity.setVersion(0L);

        // Act
        walletRepository.save(entity);

        Optional<WalletEntity> found = walletRepository.findByUserId("user-100");

        // Assert
        assertThat(found).isPresent();
        WalletEntity saved = found.get();
        assertThat(saved.getId()).isEqualTo("w-100");
        assertThat(saved.getUserId()).isEqualTo("user-100");
        assertThat(saved.getBalance()).isEqualTo(250.0);
        // version may be null or 0 depending on JPA handling in tests; assert non-null or equal when available
        assertThat(saved.getVersion()).isNotNull();
    }

    @Test
    void findByUserId_returnsEmpty_whenNoMatchingWallet() {
        Optional<WalletEntity> missing = walletRepository.findByUserId("does-not-exist");
        assertThat(missing).isEmpty();
    }
}
