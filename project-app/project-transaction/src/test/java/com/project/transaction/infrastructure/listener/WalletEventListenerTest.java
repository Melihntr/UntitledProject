package com.project.transaction.infrastructure.listener;

import com.project.common.event.UserCreatedEvent;
import com.project.transaction.infrastructure.entity.WalletEntity;
import com.project.transaction.infrastructure.repository.WalletRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class WalletEventListenerTest {

    @Mock
    private WalletRepository walletRepository;

    @InjectMocks
    private WalletEventListener listener;

    @Captor
    private ArgumentCaptor<WalletEntity> walletCaptor;

    @Test
    void onUserCreated_persistsWallet_withExpectedInitialValues() {
        // Arrange
        String newUserId = "user-abc-123";

        // Act
        listener.onUserCreated(new UserCreatedEvent(newUserId));

        // Assert
        verify(walletRepository).save(walletCaptor.capture());
        WalletEntity saved = walletCaptor.getValue();

        assertThat(saved).isNotNull();
        // id must be generated
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getId()).isNotBlank();

        // userId must match the event payload
        assertThat(saved.getUserId()).isEqualTo(newUserId);

        // initial promotional balance as defined in the listener
        assertThat(saved.getBalance()).isEqualTo(1500.0);

        // initial version must be 0L
        assertThat(saved.getVersion()).isEqualTo(0L);
    }
}