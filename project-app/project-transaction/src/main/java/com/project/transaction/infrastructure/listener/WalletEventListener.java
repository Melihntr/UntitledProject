package com.project.transaction.infrastructure.listener;

import com.project.common.event.UserCreatedEvent;
import com.project.transaction.infrastructure.entity.WalletEntity;
import com.project.transaction.infrastructure.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Infrastructure listener responsible for reacting to domain events published 
 * by other bounded contexts (e.g., the User Registration module).
 * This class facilitates decoupled, event-driven communication across the system.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class WalletEventListener {

    private final WalletRepository walletRepository;

    /**
     * Intercepts the UserCreatedEvent triggered when a new user successfully registers in the system.
     * Automatically provisions a new financial wallet for the user with an initial promotional balance.
     *
     * @param event The lightweight domain event record containing the newly created user's ID.
     */
    @EventListener
    public void onUserCreated(UserCreatedEvent event) {
        
        log.info("Intercepted UserCreatedEvent for User ID: {}. Provisioning new wallet...", event.userId());

        WalletEntity wallet = new WalletEntity();
        wallet.setUserId(event.userId()); // Extract User ID from the event payload
        
        // Initial promotional balance for testing purposes.
        // Enterprise Note: In production, this value should be externalized to application.yml 
        // (e.g., using @Value("${app.wallet.initial-balance}")).
        wallet.setBalance(1500.0); 

        // Persist the newly provisioned wallet to the database
        walletRepository.save(wallet);
        
        log.info("SUCCESS: Wallet automatically provisioned for User ID: {}", event.userId());
    }
}
