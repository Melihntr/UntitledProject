package com.project.transaction.domain.handler;

import com.project.common.domain.exception.AccessDeniedException;
import com.project.common.domain.exception.ResourceNotFoundException;
import com.project.transaction.domain.model.WalletModel;
import com.project.transaction.domain.port.TransactionPort;
import com.project.transaction.domain.usecase.DeleteWalletInput;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class DeleteWalletHandlerTest {

    @Mock
    private TransactionPort transactionPort;

    @InjectMocks
    private DeleteWalletHandler handler;

    @Test
    void handle_deletesOnlyRequestedUsersWallet() {
        WalletModel wallet = WalletModel.builder().id("wallet-1").userId("u1").build();
        when(transactionPort.findWalletById("wallet-1")).thenReturn(Optional.of(wallet));

        handler.handle(new DeleteWalletInput("wallet-1", "u1"));

        verify(transactionPort).findWalletById("wallet-1");
        verify(transactionPort).deleteWalletById("wallet-1");
    }

    @Test
    void handle_whenWalletDoesNotExist_throwsResourceNotFound() {
        when(transactionPort.findWalletById("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> handler.handle(new DeleteWalletInput("missing", "u1")))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Wallet not found with ID: missing");

        verify(transactionPort).findWalletById("missing");
    }

    @Test
    void handle_whenWalletBelongsToAnotherUser_throwsAccessDenied() {
        WalletModel wallet = WalletModel.builder().id("wallet-2").userId("owner").build();
        when(transactionPort.findWalletById("wallet-2")).thenReturn(Optional.of(wallet));

        assertThatThrownBy(() -> handler.handle(new DeleteWalletInput("wallet-2", "requester")))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("You can only delete your own wallet.");

        verify(transactionPort).findWalletById("wallet-2");
    }
}
