package com.project.user.infrastructure.repository;

import com.project.user.infrastructure.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA Repository for UserEntity.
 * Handles direct database interactions.
 */
@Repository
public interface UserRepository extends JpaRepository<UserEntity, String> {
    
    // Spring Data JPA will auto-generate this query
    boolean existsByEmail(String email);
    
    boolean existsByUsername(String username);

    List<UserEntity> findAllByIsUserDeletedFalse();

    Optional<UserEntity> findByIdAndIsUserDeletedFalse(String id);

    Optional<UserEntity> findByUsernameAndIsUserDeletedFalse(String username);
}
