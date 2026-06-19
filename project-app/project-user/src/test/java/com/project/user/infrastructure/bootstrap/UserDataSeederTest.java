package com.project.user.infrastructure.bootstrap;

import com.project.common.infrastructure.tracing.TraceIdProvider;
import com.project.user.domain.usecase.UserCreateInput;
import com.project.user.domain.model.UserModel;
import com.project.user.domain.handler.CreateUserHandler;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.MDC;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserDataSeederTest {

    @Mock
    private CreateUserHandler createUserHandler;

    @Mock
    private TraceIdProvider traceIdProvider;

    @InjectMocks
    private UserDataSeeder seeder;

    @AfterEach
    void tearDown() {
        MDC.clear();
    }

    @Test
    void runCreatesSenderAndReceiverSeedUsers() {
        when(traceIdProvider.currentTraceIdOrNew()).thenReturn("seed-trace");
        when(createUserHandler.handle(any(UserCreateInput.class)))
                .thenReturn(UserModel.builder().id("generated-id").build());

        seeder.run();

        ArgumentCaptor<UserCreateInput> inputCaptor = ArgumentCaptor.forClass(UserCreateInput.class);
        verify(createUserHandler, times(2)).handle(inputCaptor.capture());
        List<UserCreateInput> inputs = inputCaptor.getAllValues();

        assertThat(inputs).extracting(UserCreateInput::getUsername)
                .containsExactly("test_sender", "test_receiver");
        verify(traceIdProvider).currentTraceIdOrNew();
    }

    @Test
    void runStartsTraceBeforeCreatingSeedUsersAndRestoresMdc() {
        doAnswer(invocation -> {
            MDC.put("traceId", "seed-trace");
            return "seed-trace";
        }).when(traceIdProvider).currentTraceIdOrNew();
        when(createUserHandler.handle(any(UserCreateInput.class)))
                .thenAnswer(invocation -> {
                    assertThat(MDC.get("traceId")).isEqualTo("seed-trace");
                    return UserModel.builder().id("generated-id").build();
                });

        seeder.run();

        assertThat(MDC.get("traceId")).isNull();
        verify(createUserHandler, times(2)).handle(any(UserCreateInput.class));
    }

    @Test
    void runRestoresPreviousTraceIdAfterSeeding() {
        MDC.put("traceId", "existing-trace");
        when(traceIdProvider.currentTraceIdOrNew()).thenReturn("existing-trace");
        when(createUserHandler.handle(any(UserCreateInput.class)))
                .thenReturn(UserModel.builder().id("generated-id").build());

        seeder.run();

        assertThat(MDC.get("traceId")).isEqualTo("existing-trace");
    }
}
