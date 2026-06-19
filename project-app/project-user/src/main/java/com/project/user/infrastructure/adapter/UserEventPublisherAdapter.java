package com.project.user.infrastructure.adapter;

import com.project.common.event.UserCreatedEvent;
import com.project.common.tracing.TraceIdProvider;
import com.project.user.domain.port.UserEventPublisherPort;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserEventPublisherAdapter implements UserEventPublisherPort {

    private final ApplicationEventPublisher eventPublisher;
    private final TraceIdProvider traceIdProvider;

    @Override
    public void publishUserCreated(String userId) {
        eventPublisher.publishEvent(new UserCreatedEvent(userId, traceIdProvider.currentTraceIdOrNew()));
    }
}
