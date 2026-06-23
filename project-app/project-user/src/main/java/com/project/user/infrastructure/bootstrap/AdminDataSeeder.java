package com.project.user.infrastructure.bootstrap;

import com.project.common.infrastructure.security.AuthConstants;
import com.project.user.domain.port.PasswordHasherPort;
import com.project.user.infrastructure.entity.UserEntity;
import com.project.user.infrastructure.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(1)
@Slf4j
@RequiredArgsConstructor
public class AdminDataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordHasherPort passwordHasherPort;

    @Override
    public void run(String... args) {
        if (userRepository.existsByUsername("admin")) {
            log.info("seed.admin.skip username=admin");
            return;
        }

        UserEntity admin = new UserEntity();
        admin.setUsername("admin");
        admin.setEmail("admin@enterprise.com");
        admin.setPasswordHash(passwordHasherPort.hash("AdminPass123!"));
        admin.setRole(AuthConstants.ROLE_ADMIN);
        userRepository.save(admin);
        log.info("seed.admin.success username=admin");
    }
}
