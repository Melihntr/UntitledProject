package com.project.user.infrastructure.bootstrap;

import com.project.common.infrastructure.security.AuthConstants;
import com.project.user.domain.port.PasswordHasherPort;
import com.project.user.infrastructure.entity.UserEntity;
import com.project.user.infrastructure.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AdminDataSeederTest {

    private final UserRepository userRepository = mock(UserRepository.class);
    private final PasswordHasherPort passwordHasherPort = mock(PasswordHasherPort.class);
    private final AdminDataSeeder seeder = new AdminDataSeeder(userRepository, passwordHasherPort);

    @Test
    void runSkipsWhenAdminExists() {
        when(userRepository.existsByUsername("admin")).thenReturn(true);

        seeder.run();

        verify(userRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void runCreatesAdminWhenMissing() {
        when(userRepository.existsByUsername("admin")).thenReturn(false);
        when(passwordHasherPort.hash("AdminPass123!")).thenReturn("hash");

        seeder.run();

        ArgumentCaptor<UserEntity> captor = ArgumentCaptor.forClass(UserEntity.class);
        verify(userRepository).save(captor.capture());
        UserEntity admin = captor.getValue();
        assertThat(admin.getUsername()).isEqualTo("admin");
        assertThat(admin.getEmail()).isEqualTo("admin@enterprise.com");
        assertThat(admin.getPasswordHash()).isEqualTo("hash");
        assertThat(admin.getRole()).isEqualTo(AuthConstants.ROLE_ADMIN);
    }
}
