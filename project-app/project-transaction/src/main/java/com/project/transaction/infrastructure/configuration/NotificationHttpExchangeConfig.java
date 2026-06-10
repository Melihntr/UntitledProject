package com.project.transaction.infrastructure.configuration;

import com.project.transaction.infrastructure.client.NotificationHttpExchangeClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

import java.time.Duration;

@Configuration
public class NotificationHttpExchangeConfig {

    @Bean
    public NotificationHttpExchangeClient notificationHttpExchangeClient(
            @Value("${app.notification.base-url:http://localhost:8081}") String notificationBaseUrl,
            @Value("${app.notification.connect-timeout:2s}") Duration connectTimeout,
            @Value("${app.notification.read-timeout:3s}") Duration readTimeout) {

        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(connectTimeout);
        requestFactory.setReadTimeout(readTimeout);

        RestClient restClient = RestClient.builder()
                .baseUrl(notificationBaseUrl)
                .requestFactory(requestFactory)
                .build();

        HttpServiceProxyFactory factory = HttpServiceProxyFactory
                .builderFor(RestClientAdapter.create(restClient))
                .build();

        return factory.createClient(NotificationHttpExchangeClient.class);
    }
}
