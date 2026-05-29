package com.project.transaction.infrastructure.listener;

import com.project.common.event.UserCreatedEvent;
import com.project.transaction.infrastructure.entity.WalletEntity;
import com.project.transaction.infrastructure.repository.WalletRepository;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class WalletEventListener {

    private final WalletRepository walletRepository;

    public WalletEventListener(WalletRepository walletRepository) {
        this.walletRepository = walletRepository;
    }

    // Bu anotasyon sayesinde Spring bu metodu otomatik tetikler
    @EventListener
    public void onUserCreated(UserCreatedEvent event) {
        WalletEntity wallet = new WalletEntity();
        wallet.setId(UUID.randomUUID().toString());
        wallet.setUserId(event.userId()); // Gelen sinyaldeki User ID
        wallet.setBalance(1500.0); // Test için ilk açılışta 1500 TL hediye verelim!
        wallet.setVersion(0L);

        walletRepository.save(wallet);
        
        System.out.println("✅ EVENT YAKALANDI: " + event.userId() + " ID'li kullanıcı için cüzdan otomatik oluşturuldu!");
    }
}