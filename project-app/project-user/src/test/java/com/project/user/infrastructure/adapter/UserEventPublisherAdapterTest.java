package com.project.user.infrastructure.adapter;

import com.project.common.domain.event.UserCreatedEvent;
import com.project.common.infrastructure.tracing.TraceIdProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserEventPublisherAdapterTest {

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private TraceIdProvider traceIdProvider;

    @InjectMocks
    private UserEventPublisherAdapter adapter;

    @Test
    void publishUserCreated_publishesSpringEvent() {
        when(traceIdProvider.currentTraceIdOrNew()).thenReturn("trace-1");

        adapter.publishUserCreated("user-1");

        ArgumentCaptor<UserCreatedEvent> eventCaptor = ArgumentCaptor.forClass(UserCreatedEvent.class);
        verify(eventPublisher).publishEvent(eventCaptor.capture());
        assertThat(eventCaptor.getValue().userId()).isEqualTo("user-1");
        assertThat(eventCaptor.getValue().traceId()).isEqualTo("trace-1");
    }
}
