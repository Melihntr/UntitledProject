package com.project.transaction.infrastructure.repository;

import com.project.transaction.infrastructure.entity.WalletEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

/**
 * Spring Data JPA repository for managing {@link WalletEntity} persistence.
 * This infrastructure component handles database operations for user wallets,
 * abstracting away the underlying SQL queries and acting as the data source 
 * for the outbound persistence adapter.
 */
@Repository
public interface WalletRepository extends JpaRepository<WalletEntity, String> {
    
    /**
     * Retrieves a wallet entity based on its strictly associated user ID.
     * * @param userId The unique identifier of the user who owns the wallet.
     * @return An {@link Optional} containing the wallet entity if found, or empty 
     * if the user does not possess a wallet yet. Returning an Optional 
     * enforces safe null-handling in the adapter layer.
     */
    Optional<WalletEntity> findByUser_IdAndIsActiveTrue(String userId);

    Optional<WalletEntity> findByIdAndIsActiveTrue(String id);

    List<WalletEntity> findAllByIsActiveTrue();
}
