package com.project.user.domain.usecase;

import com.project.user.domain.port.UserPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class DeleteUserHandlerTest {

    @Mock
    private UserPort userPort;

    @InjectMocks
    private DeleteUserHandler handler;

    @Test
    void handle_deletesOnlyRequestedUser() {
        handler.handle("u1");

        verify(userPort).deleteUserById("u1");
    }
}
