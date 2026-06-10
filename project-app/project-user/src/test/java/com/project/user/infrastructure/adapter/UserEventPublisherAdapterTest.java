package com.project.user.infrastructure.adapter;

import com.project.common.event.UserCreatedEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class UserEventPublisherAdapterTest {

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private UserEventPublisherAdapter adapter;

    @Test
    void publishUserCreated_publishesSpringEvent() {
        adapter.publishUserCreated("user-1");

        ArgumentCaptor<UserCreatedEvent> eventCaptor = ArgumentCaptor.forClass(UserCreatedEvent.class);
        verify(eventPublisher).publishEvent(eventCaptor.capture());
        assertThat(eventCaptor.getValue().userId()).isEqualTo("user-1");
    }
}
