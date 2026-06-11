package com.project.transaction.domain.usecase;

import com.project.common.exception.ResourceNotFoundException;
import com.project.transaction.domain.port.TransactionPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
class DeleteWalletHandlerTest {

    @Mock
    private TransactionPort transactionPort;

    @InjectMocks
    private DeleteWalletHandler handler;

    @Test
    void handle_deletesOnlyRequestedUsersWallet() {
        when(transactionPort.deleteWalletByUserId("u1")).thenReturn(true);

        handler.handle("u1");

        verify(transactionPort).deleteWalletByUserId("u1");
    }

    @Test
    void handle_whenWalletDoesNotExist_throwsResourceNotFound() {
        when(transactionPort.deleteWalletByUserId("missing")).thenReturn(false);

        assertThatThrownBy(() -> handler.handle("missing"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Wallet not found for user ID: missing");

        verify(transactionPort).deleteWalletByUserId("missing");
    }
}
