package com.project.bootstrap;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mockStatic;

class ApplicationTest {

    @Test
    void applicationClassHasExpectedSpringBootAnnotations() {
        assertThat(new Application()).isNotNull();
        assertThat(Application.class.getAnnotation(SpringBootApplication.class)).isNotNull();
        assertThat(Application.class.getAnnotation(EntityScan.class)).isNotNull();
        assertThat(Application.class.getAnnotation(EnableJpaRepositories.class)).isNotNull();
    }

    @Test
    void mainDelegatesToSpringApplication() {
        String[] args = {"--test=true"};

        try (MockedStatic<SpringApplication> springApplication = mockStatic(SpringApplication.class)) {
            Application.main(args);
            springApplication.verify(() -> SpringApplication.run(Application.class, args));
        }
    }
}
