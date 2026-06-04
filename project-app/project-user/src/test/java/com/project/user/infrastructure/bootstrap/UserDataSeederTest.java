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

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * Unit tests for UserDataSeeder.
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
    void run_createsTwoUsers_publishesEvents_and_printsIds() throws Exception {
        // Prepare UserModel mocks with the expected IDs
        UserModel user1 = mock(UserModel.class);
        when(user1.getId()).thenReturn("11111111-1111-1111-1111-111111111111");

        UserModel user2 = mock(UserModel.class);
        when(user2.getId()).thenReturn("22222222-2222-2222-2222-222222222222");

        // Make createUserHandler handle(...) return user1 then user2
        when(createUserHandler.handle(any(UserCreateInput.class))).thenReturn(user1, user2);

        // Capture System.out
        PrintStream originalOut = System.out;
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));

        try {
            // Execute
            seeder.run();

        } finally {
            // Restore System.out
            System.setOut(originalOut);
        }

        // Verify two create calls with captured inputs
        verify(createUserHandler, times(2)).handle(inputCaptor.capture());
        List<UserCreateInput> capturedInputs = inputCaptor.getAllValues();
        assertThat(capturedInputs).hasSize(2);

        UserCreateInput in1 = capturedInputs.get(0);
        assertThat(in1.getId()).isEqualTo("11111111-1111-1111-1111-111111111111");
        assertThat(in1.getUsername()).isEqualTo("test_sender");
        assertThat(in1.getEmail()).isEqualTo("sender@enterprise.com");
        assertThat(in1.getRawPassword()).isEqualTo("TestPass123!");

        UserCreateInput in2 = capturedInputs.get(1);
        assertThat(in2.getId()).isEqualTo("22222222-2222-2222-2222-222222222222");
        assertThat(in2.getUsername()).isEqualTo("test_receiver");
        assertThat(in2.getEmail()).isEqualTo("receiver@enterprise.com");
        assertThat(in2.getRawPassword()).isEqualTo("TestPass123!");

        // Verify two events published and they are UserCreatedEvent instances
        verify(eventPublisher, times(2)).publishEvent(eventCaptor.capture());
        List<Object> published = eventCaptor.getAllValues();
        assertThat(published).hasSize(2);
        assertThat(published.get(0)).isInstanceOf(UserCreatedEvent.class);
        assertThat(published.get(1)).isInstanceOf(UserCreatedEvent.class);

        // Optionally assert the printed output contains the IDs
        String output = outContent.toString();
        assertThat(output).contains("SENDER ID (Use in Header & Payload) : " + user1.getId());
        assertThat(output).contains("RECEIVER ID (Use in Payload)        : " + user2.getId());
    }
}
