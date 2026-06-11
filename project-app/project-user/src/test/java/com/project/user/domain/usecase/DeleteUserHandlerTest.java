package com.project.user.domain.usecase;

import com.project.common.exception.ResourceNotFoundException;
import com.project.user.domain.port.UserPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
class DeleteUserHandlerTest {

    @Mock
    private UserPort userPort;

    @InjectMocks
    private DeleteUserHandler handler;

    @Test
    void handle_deletesOnlyRequestedUser() {
        when(userPort.deleteUserById("u1")).thenReturn(true);

        handler.handle("u1");

        verify(userPort).deleteUserById("u1");
    }

    @Test
    void handle_whenUserDoesNotExist_throwsResourceNotFound() {
        when(userPort.deleteUserById("missing")).thenReturn(false);

        assertThatThrownBy(() -> handler.handle("missing"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("User not found with ID: missing");

        verify(userPort).deleteUserById("missing");
    }
}
