package com.project.user.domain.handler;

import com.project.user.domain.usecase.UserCreateInput;
import com.project.user.domain.model.UserModel;
import com.project.user.domain.port.PasswordHasherPort;
import com.project.user.domain.port.UserEventPublisherPort;
import com.project.user.domain.port.UserPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateUserHandlerTest {

    @Mock
    private UserPort userPort;

    @Mock
    private UserEventPublisherPort eventPublisherPort;

    @Mock
    private PasswordHasherPort passwordHasherPort;

    @InjectMocks
    private CreateUserHandler handler;

    @Test
    void handle_savesUserAndPublishesCreatedEvent() {
        UserCreateInput input = UserCreateInput.builder()
                .username("alice")
                .email("alice@example.com")
                .rawPassword("secret")
                .build();

        UserModel savedUser = UserModel.builder()
                .id("user-1")
                .username("alice")
                .email("alice@example.com")
                .isActive(true)
                .build();

        when(passwordHasherPort.hash("secret")).thenReturn("hashed-secret");
        when(userPort.save(org.mockito.ArgumentMatchers.any(UserModel.class))).thenReturn(savedUser);

        UserModel result = handler.handle(input);

        ArgumentCaptor<UserModel> userCaptor = ArgumentCaptor.forClass(UserModel.class);
        verify(userPort).save(userCaptor.capture());
        UserModel userToSave = userCaptor.getValue();

        assertThat(userToSave.getId()).isNull();
        assertThat(userToSave.getUsername()).isEqualTo("alice");
        assertThat(userToSave.getEmail()).isEqualTo("alice@example.com");
        assertThat(userToSave.getPasswordHash()).isEqualTo("hashed-secret");
        assertThat(userToSave.getRole()).isEqualTo("USER");
        assertThat(userToSave.isActive()).isFalse();
        assertThat(userToSave.getCreatedAt()).isNotNull();
        assertThat(result).isSameAs(savedUser);
        verify(eventPublisherPort).publishUserCreated("user-1");
    }

    @Test
    void handle_usesGeneratedIdReturnedByPersistence() {
        UserCreateInput input = UserCreateInput.builder()
                .username("seed")
                .email("seed@example.com")
                .rawPassword("secret")
                .build();

        UserModel savedUser = UserModel.builder().id("generated-id").username("seed").build();
        when(passwordHasherPort.hash("secret")).thenReturn("hashed-secret");
        when(userPort.save(org.mockito.ArgumentMatchers.any(UserModel.class))).thenReturn(savedUser);

        UserModel result = handler.handle(input);

        assertThat(result.getId()).isEqualTo("generated-id");
        verify(eventPublisherPort).publishUserCreated("generated-id");
    }
}
