package com.project.user.domain.handler;

import com.project.user.domain.model.UserModel;
import com.project.user.domain.port.UserPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetBasicUsersHandlerTest {

    @Mock
    private UserPort userPort;

    @InjectMocks
    private GetBasicUsersHandler handler;

    @Test
    void handle_returnsUsersFromPort() {
        List<UserModel> users = List.of(UserModel.builder().id("u1").username("alice").build());
        when(userPort.getAllUsers()).thenReturn(users);

        assertThat(handler.handle(null)).isSameAs(users);
        verify(userPort).getAllUsers();
    }
}
