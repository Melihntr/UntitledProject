package com.project.user.domain.port;

public interface UserEventPublisherPort {

    void publishUserCreated(String userId);
}
