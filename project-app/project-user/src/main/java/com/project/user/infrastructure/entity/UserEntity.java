package com.project.user.infrastructure.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Database Entity representing the "users" table.
 * This class is isolated within the infrastructure layer.
 */
@Entity
@Table(name = "users")
@Getter
@Setter
public class UserEntity {

    @Id
    @Column(name = "id", updatable = false, nullable = false)
    private String id = UUID.randomUUID().toString();

    @Column(name = "username", nullable = false, unique = true)
    private String username;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "is_active", nullable = false)
    private boolean isActive;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Used for Optimistic Locking to handle concurrent transactions safely.
     */
    @Version
    @Column(name = "version")
    private Long version;

    @PrePersist
    void assignIdIfMissing() {
        if (id == null || id.isBlank()) {
            id = UUID.randomUUID().toString();
        }
    }
}
