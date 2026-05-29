package com.project.transaction.infrastructure.repository;

import com.project.transaction.infrastructure.entity.WalletEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WalletRepository extends JpaRepository<WalletEntity, String> {
    
    Optional<WalletEntity> findByUserId(String userId);
}