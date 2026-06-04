package com.project.user.infrastructure.bootstrap;

import com.project.common.event.UserCreatedEvent;
import com.project.user.domain.model.UserCreateInput;
import com.project.user.domain.model.UserModel;
import com.project.user.domain.usecase.CreateUserHandler;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for UserDataSeeder.
 * Verifies that the seeder calls the CreateUserHandler with the expected inputs
 * and publishes UserCreatedEvent objects with the IDs returned by the handler.
 */
@ExtendWith(MockitoExtension.class)
class UserDataSeederTest {

    @Mock
    private CreateUserHandler createUserHandler;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private UserDataSeeder seeder;

    @Captor
    private ArgumentCaptor<UserCreateInput> inputCaptor;

    @Captor
    private ArgumentCaptor<Object> eventCaptor;

    @Test
    void run_createsTwoUsers_andPublishesCorrespondingEvents() throws Exception {
        // Arrange: return a UserModel whose id equals the id provided in the input
        when(createUserHandler.handle(any())).thenAnswer(invocation -> {
            UserCreateInput in = invocation.getArgument(0);
            return UserModel.builder()
                    .id(in.getId())
                    .username(in.getUsername())
                    .email(in.getEmail())
                    .build();
        });

        // Act
        seeder.run();

        // Assert - CreateUserHandler called twice with expected seeded IDs & usernames
        verify(createUserHandler, times(2)).handle(inputCaptor.capture());
        List<UserCreateInput> capturedInputs = inputCaptor.getAllValues();

        assertThat(capturedInputs).hasSize(2);
        UserCreateInput i1 = capturedInputs.get(0);
        UserCreateInput i2 = capturedInputs.get(1);

        assertThat(i1.getId()).isEqualTo("11111111-1111-1111-1111-111111111111");
        assertThat(i1.getUsername()).isEqualTo("test_sender");
        assertThat(i1.getEmail()).isEqualTo("sender@enterprise.com");
        assertThat(i1.getRawPassword()).isEqualTo("TestPass123!");

        assertThat(i2.getId()).isEqualTo("22222222-2222-2222-2222-222222222222");
        assertThat(i2.getUsername()).isEqualTo("test_receiver");
        assertThat(i2.getEmail()).isEqualTo("receiver@enterprise.com");
        assertThat(i2.getRawPassword()).isEqualTo("TestPass123!");

        // Assert - events published twice with the IDs returned by the handler
        verify(eventPublisher, times(2)).publishEvent(eventCaptor.capture());
        List<Object> published = eventCaptor.getAllValues();

        assertThat(published).hasSize(2);
        assertThat(published.get(0)).isInstanceOf(UserCreatedEvent.class);
        assertThat(published.get(1)).isInstanceOf(UserCreatedEvent.class);

        UserCreatedEvent e1 = (UserCreatedEvent) published.get(0);
        UserCreatedEvent e2 = (UserCreatedEvent) published.get(1);

        assertThat(e1.getId()).isEqualTo("11111111-1111-1111-1111-111111111111");
        assertThat(e2.getId()).isEqualTo("22222222-2222-2222-2222-222222222222");

        // Ensure no unexpected interactions
        verifyNoMoreInteractions(createUserHandler, eventPublisher);
    }
}
