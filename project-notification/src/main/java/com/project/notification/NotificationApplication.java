package com.project.notification;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

/**
 * The main entry point for the independent Notification Microservice.
 * This application runs on its own embedded server (e.g., port 8081) and manages
 * its own independent database, demonstrating a decoupled microservice architecture.
 */
@SpringBootApplication
public class NotificationApplication {

    /**
     * Bootstraps and launches the Spring Boot application.
     *
     * @param args Command line arguments passed during application startup.
     */
    public static void main(String[] args) {
        SpringApplication.run(NotificationApplication.class, args);
    }

    /**
     * Registers a RestTemplate as a Spring Bean.
     * This allows the Notification Microservice to make synchronous HTTP requests
     * to external APIs (e.g., third-party SMS or Email providers) or other internal microservices if needed.
     *
     * @return A new instance of RestTemplate.
     */
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}