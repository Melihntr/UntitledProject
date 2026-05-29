package com.project.user.infrastructure.bootstrap;

import com.project.common.event.UserCreatedEvent;
import com.project.user.domain.model.UserCreateInput;
import com.project.user.domain.model.UserModel;
import com.project.user.domain.usecase.CreateUserHandler;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/**
 * Bootstraps initial data for the application on startup.
 * Creates default test users with static UUIDs and triggers the wallet creation events
 * to facilitate easy testing with the H2 in-memory database.
 */
@Component
public class UserDataSeeder implements CommandLineRunner {

    private final CreateUserHandler createUserHandler;
    private final ApplicationEventPublisher eventPublisher;

    public UserDataSeeder(CreateUserHandler createUserHandler, ApplicationEventPublisher eventPublisher) {
        this.createUserHandler = createUserHandler;
        this.eventPublisher = eventPublisher;
    }

    /**
     * Executes automatically after the Spring context is fully initialized.
     *
     * @param args Command line arguments
     */
    @Override
    public void run(String... args) {
        System.out.println("=========================================================");
        System.out.println("[INFO] Seeding test data with STATIC IDs...");

        // Create first test user (Sender) with a fixed UUID
        UserCreateInput user1Input = new UserCreateInput();
        user1Input.setId("11111111-1111-1111-1111-111111111111");
        user1Input.setUsername("test_sender");
        user1Input.setEmail("sender@enterprise.com");
        user1Input.setRawPassword("TestPass123!");
        UserModel user1 = createUserHandler.handle(user1Input);
        
        // Trigger the wallet creation event for the sender
        eventPublisher.publishEvent(new UserCreatedEvent(user1.getId()));

        // Create second test user (Receiver) with a fixed UUID
        UserCreateInput user2Input = new UserCreateInput();
        user2Input.setId("22222222-2222-2222-2222-222222222222");
        user2Input.setUsername("test_receiver");
        user2Input.setEmail("receiver@enterprise.com");
        user2Input.setRawPassword("TestPass123!");
        UserModel user2 = createUserHandler.handle(user2Input);
        
        // Trigger the wallet creation event for the receiver
        eventPublisher.publishEvent(new UserCreatedEvent(user2.getId()));

        System.out.println("[INFO] Test Data Generated Successfully.");
        System.out.println("[INFO] SENDER ID (Use in Header & Payload) : " + user1.getId());
        System.out.println("[INFO] RECEIVER ID (Use in Payload)        : " + user2.getId());
        System.out.println("=========================================================");
    }
}