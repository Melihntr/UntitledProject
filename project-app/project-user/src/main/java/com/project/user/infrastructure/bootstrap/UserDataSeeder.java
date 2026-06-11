package com.project.user.infrastructure.bootstrap;

import com.project.user.domain.model.UserCreateInput;
import com.project.user.domain.model.UserModel;
import com.project.user.domain.usecase.CreateUserHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * Bootstraps initial data for the application on startup.
 * Creates default test users with static UUIDs and triggers the wallet creation events
 * to facilitate easy testing with the H2 in-memory database.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class UserDataSeeder implements CommandLineRunner {

    private final CreateUserHandler createUserHandler;

    /**
     * Executes automatically after the Spring context is fully initialized.
     *
     * @param args Command line arguments
     */
    @Override
    public void run(String... args) {
        log.info("seed.user-data.request");

        UserModel user1 = seedUser(
                "11111111-1111-1111-1111-111111111111",
                "test_sender",
                "sender@enterprise.com"
        );
        UserModel user2 = seedUser(
                "22222222-2222-2222-2222-222222222222",
                "test_receiver",
                "receiver@enterprise.com"
        );

        log.info("seed.user-data.success senderUserId={} receiverUserId={}", user1.getId(), user2.getId());
    }

    private UserModel seedUser(String id, String username, String email) {
        UserCreateInput input = UserCreateInput.builder()
                .id(id)
                .username(username)
                .email(email)
                .rawPassword("TestPass123!")
                .build();
        return createUserHandler.handle(input);
    }
}
