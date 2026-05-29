package com.project.transaction.infrastructure.listener;

import com.project.common.event.UserCreatedEvent;
import com.project.transaction.infrastructure.entity.WalletEntity;
import com.project.transaction.infrastructure.repository.WalletRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Infrastructure listener responsible for reacting to domain events published 
 * by other bounded contexts (e.g., the User Registration module).
 * This class facilitates decoupled, event-driven communication across the system.
 */
@Component
public class WalletEventListener {

    private static final Logger logger = LoggerFactory.getLogger(WalletEventListener.class);

    private final WalletRepository walletRepository;

    // Dependency Injection via constructor
    public WalletEventListener(WalletRepository walletRepository) {
        this.walletRepository = walletRepository;
    }

    /**
     * Intercepts the UserCreatedEvent triggered when a new user successfully registers in the system.
     * Automatically provisions a new financial wallet for the user with an initial promotional balance.
     *
     * @param event The lightweight domain event record containing the newly created user's ID.
     */
    @EventListener
    public void onUserCreated(UserCreatedEvent event) {
        
        logger.info("Intercepted UserCreatedEvent for User ID: {}. Provisioning new wallet...", event.userId());

        WalletEntity wallet = new WalletEntity();
        wallet.setId(UUID.randomUUID().toString());
        wallet.setUserId(event.userId()); // Extract User ID from the event payload
        
        // Initial promotional balance for testing purposes.
        // Enterprise Note: In production, this value should be externalized to application.yml 
        // (e.g., using @Value("${app.wallet.initial-balance}")).
        wallet.setBalance(1500.0); 
        wallet.setVersion(0L);

        // Persist the newly provisioned wallet to the database
        walletRepository.save(wallet);
        
        logger.info("SUCCESS: Wallet automatically provisioned for User ID: {}", event.userId());
    }
}