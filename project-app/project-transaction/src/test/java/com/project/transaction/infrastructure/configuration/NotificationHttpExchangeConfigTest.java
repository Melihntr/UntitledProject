package com.project.transaction.infrastructure.configuration;

import com.project.transaction.infrastructure.client.NotificationHttpExchangeClient;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

class NotificationHttpExchangeConfigTest {

    @Test
    void notificationHttpExchangeClient_createsProxyClient() {
        NotificationHttpExchangeConfig config = new NotificationHttpExchangeConfig();

        NotificationHttpExchangeClient client =
                config.notificationHttpExchangeClient(
                        "http://localhost:8081", Duration.ofSeconds(2), Duration.ofSeconds(3));

        assertThat(client).isNotNull();
    }
}
