package com.project.user.infrastructure.configuration;

import com.project.user.domain.port.UserPort;
import com.project.user.domain.usecase.CreateUserHandler;
import com.project.user.domain.usecase.GetBasicUsersHandler;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class to register pure Java Domain UseCases as Spring Beans.
 */
@Configuration
public class UserBeanConfig {

    @Bean
    public CreateUserHandler createUserHandler(UserPort userPort) {
        // Spring, UserPersistenceAdapter'ı bulup bu metoda "userPort" olarak gönderecek.
        // Biz de saf Java sınıfımızı oluşturup Spring'in havuzuna (Context) bırakıyoruz.
        return new CreateUserHandler(userPort);
    }
    @Bean
    public GetBasicUsersHandler getBasicUsersHandler(UserPort userPort) {
        return new GetBasicUsersHandler(userPort);
    }
}