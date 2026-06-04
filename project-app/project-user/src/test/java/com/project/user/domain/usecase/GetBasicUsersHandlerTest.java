package com.project.user.domain.usecase;

import com.project.user.domain.model.UserModel;
import com.project.user.domain.port.UserPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetBasicUsersHandlerTest {

    @Mock
    private UserPort userPort;

    @InjectMocks
    private GetBasicUsersHandler handler;

    @Test
    void handle_returnsListFromPort() {
        // Arrange
        UserModel u1 = UserModel.builder().id("1").username("alice").build();
        UserModel u2 = UserModel.builder().id("2").username("bob").build();
        List<UserModel> expected = List.of(u1, u2);

        when(userPort.getAllUsers()).thenReturn(expected);

        // Act
        List<UserModel> result = handler.handle(null);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result).hasSize(2);
        assertThat(result).containsExactlyElementsOf(expected);

        verify(userPort, times(1)).getAllUsers();
        verifyNoMoreInteractions(userPort);
    }

    @Test
    void handle_withNoUsers_returnsEmptyList() {
        // Arrange
        when(userPort.getAllUsers()).thenReturn(List.of());

        // Act
        List<UserModel> result = handler.handle(null);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result).isEmpty();

        verify(userPort, times(1)).getAllUsers();
        verifyNoMoreInteractions(userPort);
    }
}
