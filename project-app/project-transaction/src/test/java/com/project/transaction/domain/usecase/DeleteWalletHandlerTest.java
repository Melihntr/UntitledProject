package com.project.transaction.domain.usecase;

import com.project.transaction.domain.port.TransactionPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class DeleteWalletHandlerTest {

    @Mock
    private TransactionPort transactionPort;

    @InjectMocks
    private DeleteWalletHandler handler;

    @Test
    void handle_deletesOnlyRequestedUsersWallet() {
        handler.handle("u1");

        verify(transactionPort).deleteWalletByUserId("u1");
    }
}
