package com.project.bootstrap;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.web.client.RestTemplate;

/**
 * The main entry point for the Modular Monolith Enterprise Application.
 */
@SpringBootApplication(scanBasePackages = {"com.project"})
@EntityScan(basePackages = {"com.project"}) // Hibernate'e Entity sınıflarının yerini söyler
@EnableJpaRepositories(basePackages = {"com.project"}) // Spring Data JPA'ya Repository'lerin yerini söyler
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
    /**
     * Registers RestTemplate as a Spring Bean so it can be automatically injected
     * into ExecuteTransferHandler for inter-service REST communication.
     */
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}