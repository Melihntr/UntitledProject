package com.project.user.domain.usecase;

import com.project.user.domain.model.UserCreateInput;
import com.project.user.domain.model.UserModel;
import com.project.user.domain.port.UserPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreateUserHandlerTest {

    @Mock
    private UserPort userPort;

    @InjectMocks
    private CreateUserHandler createUserHandler;

    @Captor
    private ArgumentCaptor<UserModel> userModelCaptor;

    @Test
    void handle_withProvidedId_usesProvidedId_andReturnsSavedUser() {
        // Arrange
        UserCreateInput input = UserCreateInput.builder()
                .id("fixed-id-123")
                .username("alice")
                .email("alice@example.com")
                .rawPassword("secret")
                .build();

        // Make the port return the same model it receives (simulate persistence)
        when(userPort.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        UserModel result = createUserHandler.handle(input);

        // Assert
        verify(userPort, times(1)).save(userModelCaptor.capture());
        UserModel passed = userModelCaptor.getValue();

        assertThat(passed).isNotNull();
        assertThat(passed.getId()).isEqualTo("fixed-id-123");
        assertThat(passed.getUsername()).isEqualTo("alice");
        assertThat(passed.getEmail()).isEqualTo("alice@example.com");
        assertThat(passed.isActive()).isTrue();
        assertThat(passed.getCreatedAt()).isNotNull();

        // The returned value should be what the port returned (here same as passed)
        assertThat(result).isSameAs(passed);
    }

    @Test
    void handle_withoutId_generatesId_andReturnsSavedUser() {
        // Arrange
        UserCreateInput input = UserCreateInput.builder()
                .id(null)
                .username("bob")
                .email("bob@example.com")
                .rawPassword("secret")
                .build();

        when(userPort.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        // Record time window to assert createdAt lies within it
        LocalDateTime before = LocalDateTime.now();

        // Act
        UserModel result = createUserHandler.handle(input);

        LocalDateTime after = LocalDateTime.now();

        // Assert
        verify(userPort, times(1)).save(userModelCaptor.capture());
        UserModel passed = userModelCaptor.getValue();

        assertThat(passed).isNotNull();
        assertThat(passed.getId()).isNotBlank();
        // When input.id is null, generated id should not equal null and should be returned
        assertThat(result.getId()).isEqualTo(passed.getId());

        assertThat(passed.getUsername()).isEqualTo("bob");
        assertThat(passed.getEmail()).isEqualTo("bob@example.com");
        assertThat(passed.isActive()).isTrue();

        // createdAt should be between before and after (allow small drift)
        assertThat(passed.getCreatedAt()).isNotNull();
        assertThat(passed.getCreatedAt()).isAfterOrEqualTo(before.minusSeconds(1));
        assertThat(passed.getCreatedAt()).isBeforeOrEqualTo(after.plusSeconds(1));

        // The returned result should be the same instance returned by the port
        assertThat(result).isSameAs(passed);
    }
}
