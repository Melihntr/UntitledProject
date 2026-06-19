package com.project.common.domain.event;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UserCreatedEventTest {

    @Test
    void recordCarriesUserId() {
        UserCreatedEvent event = new UserCreatedEvent("user-1", "trace-1");

        assertThat(event.userId()).isEqualTo("user-1");
        assertThat(event.traceId()).isEqualTo("trace-1");
        assertThat(event.toString()).contains("user-1");
    }
}
